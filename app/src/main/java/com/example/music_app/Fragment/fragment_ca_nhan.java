package com.example.music_app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.music_app.Activity.LoginActivity;
import com.example.music_app.Adapter.PlaylistAdapter;
import com.example.music_app.Model.Playlist;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_ca_nhan extends Fragment {

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ca_nhan, container, false);

        // Lấy ViewPager2 và TabLayout từ Activity
        viewPager2 = requireActivity().findViewById(R.id.myViewPager);
        tabLayout = requireActivity().findViewById(R.id.myTabLayout);

        // Bắt sự kiện click vào favoriteList
        View favoriteList = view.findViewById(R.id.favoriteList);
        favoriteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFavoriteFragment();
            }
        });

        // Xu ly khi nhanh nut dang xuat
        ImageView logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        // Xu ly khi nhan nut them playlist
        ImageView addPlaylistButton = view.findViewById(R.id.addPlaylistButton);
        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePlaylistDialog();
            }
        });


        // Xu ly hien username trong database
        TextView usernameText = view.findViewById(R.id.usernameText);

        // Lấy tên từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Người dùng");

        // Hiển thị lên giao diện
        usernameText.setText(username);

        loadPlaylists(view); // Hien thi danh sach phat

        // Lang nghe load lai du lieu neu co su thay doi
        getParentFragmentManager().setFragmentResultListener("playlistChange", this, (key, bundle) -> {
            boolean updated = bundle.getBoolean("playlistUpdated", false);
            boolean deleted = bundle.getBoolean("playlistDeleted", false);
            if (updated || deleted) {
                loadPlaylists(view); // gọi lại khi có thay đổi
            }
        });

        return view;
    }

    private void openFavoriteFragment() {
        // Ẩn ViewPager2 và TabLayout
        viewPager2.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);

        // Mở FragmentFavorite
        FragmentFavorite favoriteFragment = new FragmentFavorite();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, favoriteFragment);
        transaction.addToBackStack(null); // Cho phép bấm back quay lại
        transaction.commit();
    }

    // Hàm xử lý đăng xuất
    private void performLogout() {
        // Xóa SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // hoặc editor.remove("isLoggedIn"); nếu muốn giữ lại thông tin khác
        editor.apply();

        // Chuyển sang LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa hết các activity trước đó
        startActivity(intent);
    }

    private void loadPlaylists(View view) {
        RecyclerView rv = view.findViewById(R.id.rvPlaylists);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
            Call<List<Playlist>> call = apiService.getUserPlaylists(userId);

            call.enqueue(new Callback<List<Playlist>>() {
                @Override
                public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Playlist> playlists = response.body();
                        PlaylistAdapter adapter = new PlaylistAdapter(playlists);
                        rv.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "Không có danh sách phát", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Playlist>> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_playlist, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtPlaylistName = dialogView.findViewById(R.id.edtPlaylistName);
        Button btnCreate = dialogView.findViewById(R.id.btnCreatePlaylist);

        btnCreate.setOnClickListener(v -> {
            String name = edtPlaylistName.getText().toString().trim();

            if (name.isEmpty()) {
                edtPlaylistName.setError("Không được để trống");
                edtPlaylistName.requestFocus();
                return;
            }

            // Lấy userId từ SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (userId != null) {
                // Gọi API
                APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
                Call<ResponseBody> call = apiService.createPlaylist(userId, name, "");

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Tạo playlist thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadPlaylists(getView()); // Tải lại danh sách mới
                        } else {
                            Toast.makeText(getContext(), "Tạo thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}
