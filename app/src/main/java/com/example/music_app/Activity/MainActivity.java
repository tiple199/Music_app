package com.example.music_app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music_app.Adapter.MainViewPageAdapter;
import com.example.music_app.Fragment.fragment_ca_nhan;
import com.example.music_app.Fragment.fragment_trang_chu;
import com.example.music_app.Fragment.fragment_kham_pha;
import com.example.music_app.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        setupMiniPlayer();
    }

    private void setupMiniPlayer() {
        CardView miniPlayer = findViewById(R.id.miniPlayerContainer);
        ImageView thumbnail = findViewById(R.id.miniThumbnail);
        TextView title = findViewById(R.id.miniTitle);
        ImageButton playPause = findViewById(R.id.miniPlayPause);
        // Lấy bài hát đang phát từ SharedPreferences hoặc Singleton
        miniPlayer.setVisibility(View.VISIBLE);
        title.setText("Tên bài đang phát");

// play/pause
        playPause.setOnClickListener(v -> {
            // Gửi broadcast hoặc gọi đến MusicService để tạm dừng / tiếp tục
        });

// mở lại SongActivity
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SongActivity.class);
            startActivity(intent);
        });
    }

    private void init(){
        TabLayout tabLayout = findViewById(R.id.myTabLayout);
        ViewPager2 viewPager = findViewById(R.id.myViewPager);

        viewPager.setUserInputEnabled(false);

        MainViewPageAdapter adapter = new MainViewPageAdapter(this);
        adapter.addFragment(new fragment_kham_pha(), "Khám phá");
        adapter.addFragment(new fragment_trang_chu(), "Trang chủ");
        adapter.addFragment(new fragment_ca_nhan(), "Cá nhân");

        viewPager.setAdapter(adapter);

        // Sử dụng TabLayoutMediator để thiết lập tiêu đề
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getTitle(position));
            switch (position) {
                case 0: // Vị trí của tab "Khám phá"
                    // Thay R.drawable.ic_kham_pha bằng ID tài nguyên icon thực tế của bạn
                    tab.setIcon(R.drawable.icon_danhchoban);
                    break;
                // Thêm các case khác nếu bạn có nhiều tab hơn
                case 1: // Vị trí của tab "Trang chủ"
                    // Thay R.drawable.ic_trang_chu bằng ID tài nguyên icon thực tế của bạn
                    tab.setIcon(R.drawable.home);
                    break;
                case 2: // Vị trí của tab "Trang chủ"
                    // Thay R.drawable.ic_trang_chu bằng ID tài nguyên icon thực tế của bạn
                    tab.setIcon(R.drawable.user);
                    break;
            }
        }).attach();
    }
}