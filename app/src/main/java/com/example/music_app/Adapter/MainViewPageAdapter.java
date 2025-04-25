package com.example.music_app.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class MainViewPageAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> arrayFragment = new ArrayList<>();
    private ArrayList<String> arrayTitle = new ArrayList<>();

    public MainViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return arrayFragment.get(position);
    }

    @Override
    public int getItemCount() {
        return arrayFragment.size();
    }

    public void addFragment(Fragment fragment, String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        arrayFragment.add(fragment);
        arrayTitle.add(title);
    }

    // Thêm phương thức để truy cập arrayTitle từ bên ngoài
    public String getTitle(int position) {
        if (position >= 0 && position < arrayTitle.size()) {
            return arrayTitle.get(position);
        }
        return null;
    }
}