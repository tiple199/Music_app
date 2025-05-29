package com.example.music_app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Activity.AlbumSongsActivity;
import com.example.music_app.Activity.ArtistSongsActivity;
import com.example.music_app.Activity.CategoryMusicActivity;
import com.example.music_app.Activity.SearchActivity;
import com.example.music_app.Activity.SongActivity;
import com.example.music_app.Adapter.AlbumAdapter;
import com.example.music_app.Adapter.ArtistAdapter;
import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Album;
import com.example.music_app.Model.Artist;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;

import java.util.ArrayList;
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

    private RecyclerView recyclerViewAlbumHot;
    private AlbumAdapter albumAdapter;
    private RecyclerView topSongsRecyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trang_chu, container, false);



        // 🔍 Tìm kiếm: mở SearchActivity
        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.setFocusable(false);
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });

        // 🎧 Chủ đề
        TextView category1 = view.findViewById(R.id.category1);
        TextView category2 = view.findViewById(R.id.category2);
        TextView category3 = view.findViewById(R.id.category3);
        TextView category4 = view.findViewById(R.id.category4);

        category1.setOnClickListener(v -> openCategoryMusicActivity("64a111aa111aa111aa111aaa", "Nhạc trẻ", "Top nhạc trẻ hay nhất", 5));
        category2.setOnClickListener(v -> openCategoryMusicActivity("64d444dd444dd444dd444ddd", "Remix", "Top bản remix hay nhất", 5));
        category3.setOnClickListener(v -> openCategoryMusicActivity("64c333cc333cc333cc333ccc", "Chinese", "Top nhạc Hoa", 5));
        category4.setOnClickListener(v -> openCategoryMusicActivity("64b222bb222bb222bb222bbb", "KPOP", "Top bài hát KPOP", 5));

        // 🎵 Danh sách bài hát
//        songListRecycler = view.findViewById(R.id.songListRecycler);
//        songListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
//        loadSongs();
        topSongsRecyclerView = view.findViewById(R.id.songListRecycler);
        topSongsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        loadTopLikedSongs();

        // 👩‍🎤 Nghệ sĩ thịnh hành
        popularArtistsRecyclerView = view.findViewById(R.id.popularArtistsRecyclerView);
        popularArtistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadPopularArtists();

        // 💿 Album hot
        recyclerViewAlbumHot = view.findViewById(R.id.hotAlbumsRecyclerView);
        recyclerViewAlbumHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadAlbumHot();

        return view;
    }

    private void openCategoryMusicActivity(String categoryId, String categoryName, String description, int songCount) {
        Intent intent = new Intent(getActivity(), CategoryMusicActivity.class);
        intent.putExtra("CATEGORY_ID", categoryId);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.putExtra("DESCRIPTION", description);
        intent.putExtra("SONG_COUNT", songCount);
        startActivity(intent);
    }

//    private void loadSongs() {
//        APIService dataservice = APIRetrofitClient.getClient().create(APIService.class);
//        Call<List<Song>> call = dataservice.getAllSongs();
//
//        call.enqueue(new Callback<List<Song>>() {
//            @Override
//            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    songList = response.body();
//                    songAdapter = new SongAdapter(songList, position -> {
//                        Intent intent = new Intent(getActivity(), SongActivity.class);
//                        intent.putExtra("SONG_LIST", new ArrayList<>(songList));
//                        intent.putExtra("SELECTED_INDEX", position);
//                        startActivity(intent);
//                        getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.none);
//                    });
//                    songListRecycler.setAdapter(songAdapter);
//                } else {
//                    Toast.makeText(getContext(), "Không có dữ liệu bài hát", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Song>> call, Throwable t) {
//                Toast.makeText(getContext(), "Lỗi kết nối bài hát: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void loadPopularArtists() {
        APIService dataservice = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Artist>> call = dataservice.getPopularArtists();

        call.enqueue(new Callback<List<Artist>>() {
            @Override
            public void onResponse(Call<List<Artist>> call, Response<List<Artist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artistList = response.body();
                    artistAdapter = new ArtistAdapter(getContext(), artistList);

                    artistAdapter.setOnItemClickListener(artist -> {
                        Intent intent = new Intent(getContext(), ArtistSongsActivity.class);
                        intent.putExtra("ARTIST_ID", artist.get_id());
                        intent.putExtra("ARTIST_NAME", artist.getTenNgheSi());
                        intent.putExtra("ARTIST_IMAGE", artist.getHinhAnhNgheSi());
                        startActivity(intent);
                    });

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

    private void loadAlbumHot() {
        APIService dataservice = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Album>> call = dataservice.getAllAlbums();

        call.enqueue(new Callback<List<Album>>() {
            @Override
            public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Album> albumList = response.body();
                    albumAdapter = new AlbumAdapter(getContext(), albumList);

                    albumAdapter.setOnItemClickListener(album -> {
                        Intent intent = new Intent(getContext(), AlbumSongsActivity.class);
                        intent.putExtra("ALBUM_ID", album.get_id());
                        intent.putExtra("ALBUM_NAME", album.getTenAlbum());
                        intent.putExtra("ALBUM_IMAGE", album.getHinhAlbum());
                        startActivity(intent);
                    });

                    recyclerViewAlbumHot.setAdapter(albumAdapter);
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu album", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Album>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối album: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadTopLikedSongs() {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getTopLikedSongs();

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> topSongs = response.body();

                    SongAdapter adapter = new SongAdapter(topSongs, position -> {
                        Intent intent = new Intent(getActivity(), SongActivity.class);
                        intent.putExtra("SONG_LIST", new ArrayList<>(topSongs));
                        intent.putExtra("SELECTED_INDEX", position);
                        startActivity(intent);
                    });

                    topSongsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải bảng xếp hạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

}










