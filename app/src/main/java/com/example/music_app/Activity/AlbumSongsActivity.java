package com.example.music_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Server.APIRetrofitClient;
import com.example.music_app.Server.APIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumSongsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList;

    private TextView txtTenAlbum;
    private ImageView imgAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_songs);

        // Ánh xạ view
        txtTenAlbum = findViewById(R.id.txtTenAlbum);
        imgAlbum = findViewById(R.id.imgAlbum);
        recyclerView = findViewById(R.id.recyclerViewAlbumSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String albumId = intent.getStringExtra("ALBUM_ID");
        String albumName = intent.getStringExtra("ALBUM_NAME");
        String albumImage = intent.getStringExtra("ALBUM_IMAGE");

        // Hiển thị thông tin album
        txtTenAlbum.setText(albumName);
        Glide.with(this).load(albumImage).into(imgAlbum);

        // Lấy danh sách bài hát từ API
        if (albumId != null && !albumId.isEmpty()) {
            fetchSongsByAlbum(albumId);
        } else {
            Toast.makeText(this, "Không có ID album", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongsByAlbum(String albumId) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getSongsByAlbum(albumId);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();

                    songAdapter = new SongAdapter(songList, position -> {
                        Intent intent = new Intent(AlbumSongsActivity.this, SongActivity.class);
                        intent.putExtra("SONG_LIST", new ArrayList<>(songList)); // truyền danh sách
                        intent.putExtra("SELECTED_INDEX", position); // truyền vị trí được chọn
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.none);
                    });

                    recyclerView.setAdapter(songAdapter);
                } else {
                    Toast.makeText(AlbumSongsActivity.this, "Album không có bài hát nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(AlbumSongsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}



