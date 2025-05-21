package com.example.music_app.Server;

import com.example.music_app.Model.Song;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/baihat")
    Call<List<Song>> getAllSongs();
}
