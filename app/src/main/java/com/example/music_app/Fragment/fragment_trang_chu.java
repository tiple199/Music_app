package com.example.music_app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Activity.AlbumListActivity;
import com.example.music_app.Activity.ArtistListActivity;
import com.example.music_app.Activity.CategoryMusicActivity;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Server.ApiService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class fragment_trang_chu extends Fragment {
    View view;
    RecyclerView recyclerView;
    SongAdapter songAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trang_chu, container, false);

        // Ánh xạ RecyclerView
        recyclerView = view.findViewById(R.id.songListRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load dữ liệu bài hát
        loadSongsFromAPI();

        // Ánh xạ các View khác
        TextView seeMoreAlbums = view.findViewById(R.id.seeMoreAlbums);
        TextView seeMoreArtists = view.findViewById(R.id.seeMoreArtists);

        ConstraintLayout category1 = view.findViewById(R.id.category1);
        ConstraintLayout category2 = view.findViewById(R.id.category2);
        ConstraintLayout category3 = view.findViewById(R.id.category3);
        ConstraintLayout category4 = view.findViewById(R.id.category4);

        seeMoreAlbums.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AlbumListActivity.class);
            startActivity(intent);
        });

        seeMoreArtists.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ArtistListActivity.class);
            startActivity(intent);
        });

        category1.setOnClickListener(v -> openCategoryMusicActivity("Nhạc trẻ", "Top nhạc trẻ hay nhất", 10));
        category2.setOnClickListener(v -> openCategoryMusicActivity("KPOP", "Top bài hát KPOP", 15));
        category3.setOnClickListener(v -> openCategoryMusicActivity("Chinese", "Top nhạc Hoa", 8));
        category4.setOnClickListener(v -> openCategoryMusicActivity("Remix", "Top bản remix hay nhất", 12));

        return view;
    }

    private void openCategoryMusicActivity(String categoryName, String description, int songCount) {
        Intent intent = new Intent(getActivity(), CategoryMusicActivity.class);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.putExtra("DESCRIPTION", description);
        intent.putExtra("SONG_COUNT", songCount);
        startActivity(intent);
    }

    private void loadSongsFromAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/") // IP localhost trên máy ảo
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        api.getAllSongs().enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songAdapter = new SongAdapter(response.body());
                    recyclerView.setAdapter(songAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Không kết nối được: " + t.getMessage());
            }
        });
    }
}


