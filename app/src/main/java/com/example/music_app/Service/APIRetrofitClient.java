package com.example.music_app.Service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIRetrofitClient {
    // Thay thế bằng URL gốc của API của bạn
//    private static final String BASE_URL = "http://10.0.2.2:8080/api/"; // Đã có dấu / ở cuối -> Tốt!
    private static final String BASE_URL = "http://192.168.0.197:8080/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}