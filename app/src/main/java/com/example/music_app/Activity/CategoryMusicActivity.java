package com.example.music_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryMusicActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView imgCategory;
    private TextView tvCategoryName, tvDescription, tvSongCount;
    private RecyclerView recyclerViewSongs;

    private String categoryId;
    private List<Song> songList;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_music);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        imgCategory = findViewById(R.id.imgCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvDescription = findViewById(R.id.tvDescription);
        tvSongCount = findViewById(R.id.tvSongCount);
        recyclerViewSongs = findViewById(R.id.recyclerViewSongs);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("CATEGORY_ID");
        String categoryName = intent.getStringExtra("CATEGORY_NAME");
        String description = intent.getStringExtra("DESCRIPTION");
        int songCount = intent.getIntExtra("SONG_COUNT", 0);

        // Hiển thị dữ liệu
        tvCategoryName.setText(categoryName);
        tvDescription.setText(description);
        tvSongCount.setText(songCount + " bài hát");

        // Hình ảnh tương ứng với category
        setCategoryImage(categoryName);

        // Gọi API lấy bài hát
        loadSongsByCategory();
        fetchSongsByTopic(categoryId);
//        loadAllSongs();

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    private void setCategoryImage(String categoryName) {
        switch (categoryName) {
            case "Nhạc trẻ":
                imgCategory.setImageResource(R.drawable.ic_music);
                break;
            case "KPOP":
                imgCategory.setImageResource(R.drawable.ic_kpop);
                break;
            case "Chinese":
                imgCategory.setImageResource(R.drawable.ic_chinese);
                break;
            case "Remix":
                imgCategory.setImageResource(R.drawable.ic_remix);
                break;
            default:
                imgCategory.setImageResource(R.drawable.ic_music);
        }
    }

    private void loadSongsByCategory() {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getSongsByCategory(categoryId); // Gọi API với id

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body(); // Lưu lại để truyền sang SongActivity

                    songAdapter = new SongAdapter(songList, position -> {
                        Intent intent = new Intent(CategoryMusicActivity.this, SongActivity.class);
                        intent.putExtra("SONG_LIST", new ArrayList<>(songList)); // truyền danh sách bài hát
                        intent.putExtra("SELECTED_INDEX", position); // truyền index bài hát được chọn
                        startActivity(intent);
                    });

                    recyclerViewSongs.setLayoutManager(new LinearLayoutManager(CategoryMusicActivity.this));
                    recyclerViewSongs.setAdapter(songAdapter);
                } else {
                    Toast.makeText(CategoryMusicActivity.this, "Không có bài hát trong chủ đề này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(CategoryMusicActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllSongs() {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getAllSongs();

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songList = response.body();
                    SongAdapter adapter = new SongAdapter(songList, position -> {
                        // TODO: xử lý khi chọn bài hát nếu muốn
                    });
                    recyclerViewSongs.setLayoutManager(new LinearLayoutManager(CategoryMusicActivity.this));
                    recyclerViewSongs.setAdapter(adapter);
                } else {
                    Toast.makeText(CategoryMusicActivity.this, "Không có dữ liệu bài hát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(CategoryMusicActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchSongsByTopic(String theLoaiId) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getSongsByCategory(theLoaiId); // Đúng tên API

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();

                    songAdapter = new SongAdapter(songList, position -> {
                        Intent intent = new Intent(CategoryMusicActivity.this, SongActivity.class);
                        intent.putExtra("SONG_LIST", new ArrayList<>(songList));
                        intent.putExtra("SELECTED_INDEX", position);
                        startActivity(intent);
                    });

                    recyclerViewSongs.setAdapter(songAdapter);

                } else {
                    Toast.makeText(CategoryMusicActivity.this, "Không có bài hát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(CategoryMusicActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}



