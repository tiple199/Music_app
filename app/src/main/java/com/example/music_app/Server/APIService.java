package com.example.music_app.Server;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;


import com.example.music_app.Model.Album;
import com.example.music_app.Model.Artist;
import com.example.music_app.Model.Song;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {
    @POST("register")
    @FormUrlEncoded
    Call<String> registerUser(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );

    @POST("login")
    @FormUrlEncoded
    Call<ResponseBody> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );
    @GET("songs")  // endpoint backend trả danh sách bài hát
    Call<List<Song>> getAllSongs();

    @GET("favorites")
    Call<List<Song>> getFavoriteSongs(@Query("userId") String userId);

    @GET("artists")
    Call<List<Artist>> getPopularArtists();

    @GET("song/random")
    Call<Song> getRandomSong();

    @GET("albums")
    Call<List<Album>> getAllAlbums();
    @GET("songs/by-theloai/{theLoaiId}")
    Call<List<Song>> getSongsByCategory(@Path("theLoaiId") String theLoaiId);

    @GET("songs/byArtist/{artistId}")
    Call<List<Song>> getSongsByArtist(@Path("artistId") String artistId);

    @GET("songs/byAlbum/{albumId}")
    Call<List<Song>> getSongsByAlbum(@Path("albumId") String albumId);
    @GET("songs/search")
    Call<List<Song>> searchSongs(@Query("keyword") String keyword);

    @GET("songs/top-liked-songs")
    Call<List<Song>> getTopLikedSongs();



}


