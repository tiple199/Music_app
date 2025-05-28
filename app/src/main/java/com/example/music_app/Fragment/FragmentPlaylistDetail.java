package com.example.music_app.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Playlist;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Server.APIService;
import com.example.music_app.Server.APIRetrofitClient;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPlaylistDetail extends Fragment {

    private TextView tvTitle;
    private RecyclerView rvSongs;
    private String playlistName, playlistId;

    public FragmentPlaylistDetail() {}

    public static FragmentPlaylistDetail newInstance(String playlistId, String playlistName) {
        FragmentPlaylistDetail fragment = new FragmentPlaylistDetail();
        Bundle args = new Bundle();
        args.putString("playlistId", playlistId);
        args.putString("playlistName", playlistName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        rvSongs = view.findViewById(R.id.rvSongsPlaylist);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            playlistId = getArguments().getString("playlistId", "");
            playlistName = getArguments().getString("playlistName", "");
            tvTitle.setText(playlistName);
            fetchSongsForPlaylist(); // Gọi bằng id
        }

        // Bắt sự kiện click nút back
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi click, quay trở lại fragment trước
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ImageButton btnMore = view.findViewById(R.id.btnMore);
        btnMore.setOnClickListener(v -> showPopupMenu(v));


        return view;
    }

    // Ham khi click vào nut 3 cham thi hien len menu
    private void showPopupMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_playlist_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit) {
                showEditDialog(); // Gọi hàm sửa tên
                return true;
            } else if (id == R.id.menu_delete) {
                confirmDelete(); // Gọi hàm xóa playlist
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Ham hien len dialog de sua ten
    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sửa tên Playlist");

        final EditText input = new EditText(getContext());
        input.setHint("Nhập tên mới");
        input.setText(playlistName); // tên hiện tại
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
                Call<Playlist> call = apiService.updatePlaylistName(playlistId, newName);

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã đổi tên", Toast.LENGTH_SHORT).show();
                            tvTitle.setText(newName);
                            playlistName = newName;

                            // Gửi kết quả về fragment trước
                            Bundle result = new Bundle();
                            result.putBoolean("playlistUpdated", true);
                            getParentFragmentManager().setFragmentResult("playlistChange", result);

                        } else {
                            Toast.makeText(getContext(), "Lỗi khi đổi tên", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Playlist> call, Throwable t) {
                        Toast.makeText(getContext(), "Kết nối thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Ham xoa playlist cua user
    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Playlist")
                .setMessage("Bạn có chắc muốn xóa?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
                    Call<ResponseBody> call = apiService.deletePlaylist(playlistId);
                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa playlist", Toast.LENGTH_SHORT).show();

                                // Gửi kết quả về fragment trước
                                Bundle result = new Bundle();
                                result.putBoolean("playlistDeleted", true);
                                getParentFragmentManager().setFragmentResult("playlistChange", result);

                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Ham de lay ra bai hat trong playlist cua user
    private void fetchSongsForPlaylist() {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Song>> call = apiService.getSongsByPlaylistId(playlistId);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songList = response.body();
                    SongAdapter songAdapter = new SongAdapter(songList, null);
                    rvSongs.setAdapter(songAdapter);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy bài hát trong playlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
