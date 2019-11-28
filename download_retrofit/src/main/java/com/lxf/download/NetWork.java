package com.lxf.download;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class NetWork {
    private static NetWork instance;
    private Api api;

    private NetWork() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(8000, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Response response = chain.proceed(chain.request());
                        return response
                                .newBuilder()
                                .body(new FileResponseBody(response))
                                .build();
                    }
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://baidu.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        api = retrofit.create(Api.class);
    }

    static NetWork getInstance() {
        if (instance == null) {
            synchronized (NetWork.class) {
                if (instance == null) {
                    instance = new NetWork();
                }
            }
        }
        return instance;
    }

    Observable<Response<Void>> fileLength(String url) {
        return api.fileLength(url);
    }

    Observable<ResponseBody> down(String range, String url) {
        return api.down(range, url);
    }
}
