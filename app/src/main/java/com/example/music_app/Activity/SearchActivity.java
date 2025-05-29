package com.example.music_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIService;
import com.example.music_app.Service.APIRetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearchInput;
    private RecyclerView rvSearchResults;
    private List<Song> songList;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo view
        etSearchInput = findViewById(R.id.etSearchInput);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        // Setup RecyclerView
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(songList, new SongAdapter.MyClickListenner() {
            @Override
            public void onItemClick(int position) {
                // Xử lý khi chọn bài hát, ví dụ mở SongActivity
                Song song = songList.get(position);
                Intent intent = new Intent(SearchActivity.this, SongActivity.class);
                intent.putExtra("song", song); // Song phải implement Serializable hoặc Parcelable
                startActivity(intent);
            }
        });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(songAdapter);

        // Xử lý khi nhấn nút "Search" trên bàn phím
        etSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = etSearchInput.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    fetchSongsSearch(keyword);
                } else {
                    Toast.makeText(SearchActivity.this, "Nhập từ khóa để tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });


    }

    private void searchSongs(String keyword) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.searchSongs(keyword);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList.clear();
                    songList.addAll(response.body());
                    songAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchActivity.this, "Không tìm thấy bài hát nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchSongsSearch(String keyword) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.searchSongs(keyword);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();

                    songAdapter = new SongAdapter(songList, position -> {
                        Intent intent = new Intent(SearchActivity.this, SongActivity.class);
                        intent.putExtra("SONG_LIST", new ArrayList<>(songList)); // truyền danh sách bài hát
                        intent.putExtra("SELECTED_INDEX", position);            // truyền vị trí bài hát
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.none);
                    });

                    rvSearchResults.setAdapter(songAdapter);
                } else {
                    Toast.makeText(SearchActivity.this, "Không tìm thấy bài hát nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}

