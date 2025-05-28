package com.example.music_app.Activity;

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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistSongsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList;

    private TextView txtTenNgheSi, txtNguoiNghe;
    private ImageView imgArtist;

    private String artistName, artistImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);

        recyclerView = findViewById(R.id.recyclerViewSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        txtTenNgheSi = findViewById(R.id.txtTenNgheSi);
        txtNguoiNghe = findViewById(R.id.txtNguoiNghe);
        imgArtist = findViewById(R.id.imgArtist);

        String artistId = getIntent().getStringExtra("ARTIST_ID");
        artistName = getIntent().getStringExtra("ARTIST_NAME");
        artistImageUrl = getIntent().getStringExtra("ARTIST_IMAGE");

        if (artistId == null || artistId.isEmpty()) {
            Toast.makeText(this, "Không có ID nghệ sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set thông tin nghệ sĩ từ Intent
        txtTenNgheSi.setText(artistName);
        txtNguoiNghe.setText("79.3 tr người nghe hàng tháng");
        Glide.with(this).load(artistImageUrl).into(imgArtist);

        // Gọi API lấy danh sách bài hát
        fetchSongsByArtist(artistId);
    }

    private void fetchSongsByArtist(String artistId) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getSongsByArtist(artistId);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();
                    songAdapter = new SongAdapter(songList, position -> {
                        // Tuỳ chọn: mở SongActivity
                    });
                    recyclerView.setAdapter(songAdapter);
                } else {
                    Toast.makeText(ArtistSongsActivity.this, "Không có bài hát nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(ArtistSongsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}





