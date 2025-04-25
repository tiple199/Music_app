package com.example.music_app.Server;

import com.example.music_app.Model.Quangcao;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Dataservice {
    @GET("songbanner.php")
    Call<List<Quangcao>> GetDataBanner();
}
