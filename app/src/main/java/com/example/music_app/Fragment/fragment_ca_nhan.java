package com.example.music_app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
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
}
