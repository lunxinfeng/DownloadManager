package com.lxf.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

public class DownloadManager {
    private static long downLength = 0;
    private static final int CONNECT_TIME_OUT = 60 * 1000;
    private static int threadNum = 3;
    private static ExecutorService executorService = Executors.newFixedThreadPool(threadNum + 1);


    public static void down(String url, String filePath, DownloadListener listener) {
        executorService.execute(() -> {
            try {
                long totalLength = getLength(url);
                if (listener!=null)
                    listener.onStart(totalLength);
                long blockSize = totalLength / threadNum;
                for (int i = 0; i < threadNum; i++) {
                    long start = i * blockSize;
                    long end = (i + 1) * blockSize - 1;
                    if (threadNum == i + 1) {
                        end = totalLength;
                    }
                    long finalEnd = end;
                    executorService.execute(() -> {
                        try {
                            downPart(url, filePath, start, finalEnd, totalLength, listener);
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (listener!=null)
                                listener.onFail();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener!=null)
                    listener.onFail();
            }
        });
    }

    private static long getLength(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return connection.getContentLengthLong();
        }
        return 0;
    }

    private static void downPart(String urlPath, String filePath, long start, long end, long totalLength, DownloadListener listener) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
            RandomAccessFile file = new RandomAccessFile(filePath, "rwd");
            file.seek(start);
            byte[] b = new byte[1024 * 1024];
            int len;
            while ((len = bufferedInputStream.read(b)) > -1) { //循环写入
                file.write(b, 0, len);
                synchronized (DownloadManager.class) {//此处涉及到变量同步
                    DownloadManager.downLength = DownloadManager.downLength + len; //计算当前下载了多少
                    if (listener != null) {
                        int progress = (int) (downLength * 100 / totalLength);
                        listener.onProgress(progress);
                        if (progress >= 100)
                            listener.onComplete();
                    }
                }
            }
            file.close();
            bufferedInputStream.close();
            System.out.println("部分下载完成,开始位置" + start + ",结束位置" + end);
        }
    }
}
