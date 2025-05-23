package com.example.music_app.Server;

import com.example.music_app.Model.Song;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

    @GET("/api/favorites")
    Call<List<Song>> getFavoriteSongs(@Query("userId") String userId);
}
