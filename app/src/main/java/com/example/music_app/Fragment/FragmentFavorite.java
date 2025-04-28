package com.example.music_app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.music_app.R;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

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
