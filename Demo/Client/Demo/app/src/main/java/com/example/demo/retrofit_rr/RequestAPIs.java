package com.example.demo.retrofit_rr;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RequestAPIs {
    @Headers("Content-Type: application/json")
    @POST("init_file")
    Call<ResponseData> initFile(@Body InitFileRequestData req);

    @Headers("Content-Type: application/json")
    @POST("create_file")
    Call<ResponseData> createFile(@Body CreateFileRequestData req);
}
