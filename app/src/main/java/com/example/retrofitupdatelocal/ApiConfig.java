package com.example.retrofitupdatelocal;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

interface ApiConfig {
    @Streaming
    @Multipart
    @POST("/upload")
    Call<ServerResponse> uploadFile(@Part MultipartBody.Part file,
                                    @Part("file") RequestBody name);

    @Streaming
    @Multipart
    @POST("/upload")
    Call<ServerResponse> uploadMulFile(@Part MultipartBody.Part file1,
                                       @Part MultipartBody.Part file2);
}
