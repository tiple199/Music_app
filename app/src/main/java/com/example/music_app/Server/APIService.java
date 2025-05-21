package com.example.music_app.Server;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {
    @POST("/api/register")
    @FormUrlEncoded
    Call<String> registerUser(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );

    @POST("/api/login")
    @FormUrlEncoded
    Call<ResponseBody> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );
}
