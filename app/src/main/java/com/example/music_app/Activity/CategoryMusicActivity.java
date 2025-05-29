package com.example.music_app.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Song;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.R;
import android.content.Intent;


import java.util.ArrayList;
import java.util.List;


public class CategoryMusicActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView imgCategory;
    private TextView tvCategoryName, tvDescription, tvSongCount;
    private RecyclerView recyclerViewSongs;

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
        String categoryName = intent.getStringExtra("CATEGORY_NAME");
        String description = intent.getStringExtra("DESCRIPTION");
        int songCount = intent.getIntExtra("SONG_COUNT", 0);

        // Hiển thị dữ liệu
        tvCategoryName.setText(categoryName);
        tvDescription.setText(description);
        tvSongCount.setText(songCount + " bài hát");

        // Thiết lập hình ảnh tùy theo category (cần thêm logic riêng)
        setCategoryImage(categoryName);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Sự kiện nút back
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

    private void setupRecyclerView() {
        // Lấy danh sách bài hát theo category (cần implement)
        List<Song> songList = getSongsByCategory();

        SongAdapter adapter = new SongAdapter(songList,position -> {

        }, null, null);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSongs.setAdapter(adapter);
    }

    private List<Song> getSongsByCategory() {
        // Triển khai logic lấy danh sách bài hát từ server/local theo category
        return new ArrayList<>();
    }
}

