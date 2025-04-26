package com.example.music_app.Server;

import com.example.music_app.Model.Quangcao;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interface for defining API endpoints to fetch data.
 */
public interface Dataservice {
    @GET("songbanner.php?i=1")
    Call<List<Quangcao>> GetDataBanner();
}