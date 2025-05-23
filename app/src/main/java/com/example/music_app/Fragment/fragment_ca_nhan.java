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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.example.music_app.Activity.LoginActivity;
import com.example.music_app.R;
import com.google.android.material.tabs.TabLayout;

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

        // Xu ly hien username len trang ca nhan
        TextView usernameText = view.findViewById(R.id.usernameText);

        // Lấy tên từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Người dùng");

        // Hiển thị lên giao diện
        usernameText.setText(username);



        return view;
    }

    private void openFavoriteFragment() {
        // Ẩn ViewPager2 và TabLayout
        viewPager2.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);

        // Mở FragmentFavorite
        FragmentFavorite favoriteFragment = new FragmentFavorite();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, favoriteFragment); // ⚡ Replace vào màn hình chính
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

}
