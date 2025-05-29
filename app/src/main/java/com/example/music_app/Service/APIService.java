package com.example.music_app.Service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;


import com.example.music_app.Model.Album;
import com.example.music_app.Model.Artist;
import com.example.music_app.Model.Favorite;
import com.example.music_app.Model.Playlist;
import com.example.music_app.Model.Song;
import com.google.gson.JsonObject;

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

    @FormUrlEncoded
    @POST("favorites")
    Call<Favorite> addFavorite(@Field("userId") String userId, @Field("songId") String songId);

    //Dùng @HTTP vì Retrofit không hỗ trợ @DELETE với @Field mặc định.
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "favorites", hasBody = true)
    Call<Void> removeFavorite(
            @Field("userId") String userId,
            @Field("songId") String songId
    );

    @GET("favorites/check")
    Call<JsonObject> isSongFavorited(
            @Query("userId") String userId,
            @Query("songId") String songId
    );

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

    @HTTP(method = "DELETE", path = "playlist/remove-song", hasBody = true)
    @FormUrlEncoded
    Call<ResponseBody> removeSongFromPlaylist(
            @Field("playlistId") String playlistId,
            @Field("songId") String songId
    );

    @FormUrlEncoded
    @POST("playlist/add-song")
    Call<ResponseBody> addSongToPlaylist(
            @Field("playlistId") String playlistId,
            @Field("songId") String songId
    );

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


