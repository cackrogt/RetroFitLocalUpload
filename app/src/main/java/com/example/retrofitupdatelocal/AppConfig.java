package com.example.retrofitupdatelocal;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class AppConfig {

    //private static String BASE_URL = "http://mushtaq.16mb.com/";

    public static String BASE_URL = "https://80ee-122-171-21-109.ngrok-free.app";// https://e15d-122-171-21-109.ngrok-free.app


    static Retrofit getRetrofit(String serverUrl) {
        if(serverUrl == null || serverUrl == ""){
          return new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
