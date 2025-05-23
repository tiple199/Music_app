package com.example.music_app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Activity.AlbumListActivity;
import com.example.music_app.Activity.ArtistListActivity;
import com.example.music_app.Activity.CategoryMusicActivity;
import com.example.music_app.Adapter.ArtistAdapter;
import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Artist;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Server.APIService;
import com.example.music_app.Server.APIUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_trang_chu extends Fragment {
    private View view;
    private RecyclerView songListRecycler;
    private SongAdapter songAdapter;
    private List<Song> songList;

    private RecyclerView popularArtistsRecyclerView;
    private ArtistAdapter artistAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trang_chu, container, false);

        // Ánh xạ TextView
        TextView seeMoreAlbums = view.findViewById(R.id.seeMoreAlbums);
        TextView seeMoreArtists = view.findViewById(R.id.seeMoreArtists);

        // Ánh xạ layout chủ đề nhạc
        ConstraintLayout category1 = view.findViewById(R.id.category1);
        ConstraintLayout category2 = view.findViewById(R.id.category2);
        ConstraintLayout category3 = view.findViewById(R.id.category3);
        ConstraintLayout category4 = view.findViewById(R.id.category4);

        // Thiết lập sự kiện click cho các danh mục
        category1.setOnClickListener(v -> openCategoryMusicActivity("Nhạc trẻ", "Top nhạc trẻ hay nhất", 10));
        category2.setOnClickListener(v -> openCategoryMusicActivity("KPOP", "Top bài hát KPOP", 15));
        category3.setOnClickListener(v -> openCategoryMusicActivity("Chinese", "Top nhạc Hoa", 8));
        category4.setOnClickListener(v -> openCategoryMusicActivity("Remix", "Top bản remix hay nhất", 12));

        // Sự kiện "Xem thêm"
        seeMoreAlbums.setOnClickListener(v -> startActivity(new Intent(getContext(), AlbumListActivity.class)));
        seeMoreArtists.setOnClickListener(v -> startActivity(new Intent(getContext(), ArtistListActivity.class)));

        // Load danh sách bài hát
        songListRecycler = view.findViewById(R.id.songListRecycler);
        songListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        loadSongs();

        // Load danh sách nghệ sĩ thịnh hành
        popularArtistsRecyclerView = view.findViewById(R.id.popularArtistsRecyclerView);
        popularArtistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadPopularArtists();

        return view;
    }

    private void openCategoryMusicActivity(String categoryName, String description, int songCount) {
        Intent intent = new Intent(getActivity(), CategoryMusicActivity.class);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.putExtra("DESCRIPTION", description);
        intent.putExtra("SONG_COUNT", songCount);
        startActivity(intent);
    }

    private void loadSongs() {
        APIService dataservice = APIUtil.getService();
        Call<List<Song>> call = dataservice.getAllSongs();

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();
                    songAdapter = new SongAdapter(songList);
                    songListRecycler.setAdapter(songAdapter);
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu bài hát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối bài hát: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPopularArtists() {
        APIService dataservice = APIUtil.getService();
        Call<List<Artist>> call = dataservice.getPopularArtists();

        call.enqueue(new Callback<List<Artist>>() {
            @Override
            public void onResponse(Call<List<Artist>> call, Response<List<Artist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artistList = response.body();
                    artistAdapter = new ArtistAdapter(getContext(), artistList);
                    popularArtistsRecyclerView.setAdapter(artistAdapter);
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu nghệ sĩ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Artist>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối nghệ sĩ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}








