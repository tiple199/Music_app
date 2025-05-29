package com.example.music_app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_app.Activity.SongActivity;
import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.Model.Song;
import com.example.music_app.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentFavorite extends Fragment {

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Lấy ViewPager2 và TabLayout từ Activity
        viewPager2 = requireActivity().findViewById(R.id.myViewPager);
        tabLayout = requireActivity().findViewById(R.id.myTabLayout);

        // Bắt sự kiện click nút back
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi click, quay trở lại fragment trước
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Xu ly hien username trong database
        TextView usernameText = view.findViewById(R.id.tvUsername);

        // Lấy tên từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Người dùng");

        // Hiển thị lên giao diện
        usernameText.setText(username);

        // Xu ly phan hien thi bai hat yeu thich cua nguoi dung
        RecyclerView recyclerView = view.findViewById(R.id.rvSongsFavotite);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy userId
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
            Call<List<Song>> call = apiService.getFavoriteSongs(userId);

            call.enqueue(new Callback<List<Song>>() {
                @Override
                public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Song> songList = response.body();
                        SongAdapter adapter = new SongAdapter(songList, position -> {
                            Intent intent = new Intent(getActivity(), SongActivity.class);
                            intent.putExtra("SONG_LIST", new ArrayList<>(songList));
                            intent.putExtra("SELECTED_INDEX", position);
                            startActivity(intent);
                            requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.none);
                        }, "favorite", null);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "Không có bài hát yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Song>> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Khi FragmentFavorite đóng -> Hiện lại ViewPager2 + TabLayout
        viewPager2.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
    }
}
