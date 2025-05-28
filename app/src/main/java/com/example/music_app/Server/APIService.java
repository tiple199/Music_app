package com.example.music_app.Server;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import com.example.music_app.Model.Artist;
import com.example.music_app.Model.Playlist;
import com.example.music_app.Model.Song;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @GET("user-playlists")
    Call<List<Playlist>> getUserPlaylists(@Query("userId") String userId);

    @POST("user-playlists")
    @FormUrlEncoded
    Call<ResponseBody> createPlaylist(
            @Field("userId") String userId,
            @Field("ten") String ten,
            @Field("hinhNen") String hinhNen
    );

    @GET("playlists/{playlistId}/songs")
    Call<List<Song>> getSongsByPlaylistId(@Path("playlistId") String playlistId);

    @PUT("playlists/{id}")
    @FormUrlEncoded
    Call<Playlist> updatePlaylistName(@Path("id") String playlistId, @Field("newName") String newName);

    @DELETE("playlists/{id}")
    Call<ResponseBody> deletePlaylist(@Path("id") String playlistId);



}


