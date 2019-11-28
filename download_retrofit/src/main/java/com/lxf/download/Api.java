package com.lxf.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;

interface Api {

    @HEAD
    Observable<Response<Void>> fileLength(@Url String url);

    @Streaming
    @GET
    Observable<ResponseBody> down(@Header("Range") String range, @Url String url);
}
