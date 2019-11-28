package com.lxf.download;


import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Response;

/**
 * app更新管理类
 * Created by lxf on 2017/3/3.
 */
public class DownloadManager {

    private Disposable disposableDownload;
    private Disposable disposableListener;
    private long downloadLength;
    private long totalLength;

    private DownloadListener listener;

    public DownloadManager() {
        RxBus.getDefault().toObservable(FileLoadingBean.class)
                .subscribe(new Observer<FileLoadingBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableListener = d;
                    }

                    @Override
                    public void onNext(FileLoadingBean value) {
                        int progress =
                                (int) Math.round((value.getBytesReaded() + downloadLength) / (double) totalLength * 100);
                        if (listener != null)
                            listener.onProgress(progress);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (disposableListener != null)
                            disposableListener.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (disposableListener != null)
                            disposableListener.dispose();
                    }
                });
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public interface DownloadListener {
        void onStart(long totalLength);

        void onProgress(int progress);

        void onComplete();

        void onFail();
    }

    /**
     * 是否需要更新,需要则下载
     *
     * @param url     新版本地址
     * @param apkPath 本地apk保存路径
     */
    public void down(final String url, final String apkPath) {
        NetWork.getInstance()
                .fileLength(url)
                .map(new Function<Response<Void>, Long>() {
                    @Override
                    public Long apply(Response<Void> response) {
                        if (response == null || response.headers() == null || response.headers().get("Content-Length") == null)
                            return 0L;
                        totalLength = Long.parseLong(response.headers().get("Content-Length"));
                        System.out.println("request file length: " + totalLength);
                        return totalLength;
                    }
                })
                .flatMap(new Function<Long, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(Long contentLength) {
                        if (contentLength == 0) {
                            return Observable.error(new RuntimeException("请求异常"));
                        }
                        File file = new File(apkPath);
                        if (!file.exists()) {//文件不存在
                            downloadLength = 0;
                        } else {//文件存在
                            downloadLength = file.length();
                        }
                        System.out.println("local length : " + downloadLength);
                        if (downloadLength > contentLength) {
                            //异常，删除文件重新下
                            file.delete();
                            downloadLength = 0;
                        } else if (downloadLength == contentLength) {
                            //下载已经完成
                            return Observable.empty();
                        }
                        return NetWork.getInstance().down("bytes=" + downloadLength + "-" + contentLength, url);
                    }
                })
                .map(new Function<ResponseBody, BufferedSource>() {
                    @Override
                    public BufferedSource apply(ResponseBody responseBody) {
                        long requestLength = responseBody.contentLength();
                        if (listener != null)
                            listener.onStart(requestLength);

                        totalLength = downloadLength + requestLength;
                        System.out.println("total length : " + downloadLength + "+" + responseBody.contentLength() + "=" + totalLength);
                        return responseBody.source();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<BufferedSource>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("UpdateManager.onSubscribe");
                        disposableDownload = d;
                    }

                    @Override
                    public void onNext(BufferedSource bufferedSource) {
                        System.out.println("UpdateManager.onNext");
                        try {
                            System.out.println("writing to file");
                            writeFile(bufferedSource, new File(apkPath));
                        } catch (IOException e) {
                            onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("UpdateManager.onError : " + e.getMessage());
                        stop();
                        if (listener != null)
                            listener.onFail();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("UpdateManager.onComplete");
                        stop();
                        if (listener != null)
                            listener.onComplete();
                        //安装apk
//                        installApk(apkPath,context);
                    }
                });
    }

    public void stop() {
        if (disposableDownload != null)
            disposableDownload.dispose();
    }

    /**
     * 写入文件
     */
    private void writeFile(BufferedSource source, File file) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(Okio.appendingSink(file));
        bufferedSink.writeAll(source);

        bufferedSink.close();
        source.close();
    }
}
