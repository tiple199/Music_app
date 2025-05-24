package com.example.music_app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm Log để kiểm tra nếu cần
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music_app.Adapter.MainViewPageAdapter;
import com.example.music_app.Fragment.fragment_ca_nhan;
import com.example.music_app.Fragment.fragment_trang_chu;
import com.example.music_app.Fragment.fragment_kham_pha;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.MusicService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity"; // Tag cho logging

    private View miniPlayerContainer;
    private TextView miniTitle;
    private ImageButton miniPlayPause;
    private TabLayout tabLayout; // Khai báo tabLayout ở mức class
    private ViewPager2 viewPager; // Có thể cần nếu muốn lấy vị trí từ ViewPager2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần UI và listeners
        initializeUIComponents(); // Gom việc findViewById vào một hàm
        initTabs();
        initMiniPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayerState(); // Cập nhật trạng thái mini player khi activity resume
    }

    private void initializeUIComponents() {
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        miniTitle = findViewById(R.id.miniTitle);
        miniPlayPause = findViewById(R.id.miniPlayPause);
        tabLayout = findViewById(R.id.myTabLayout); // Gán vào biến class
        viewPager = findViewById(R.id.myViewPager); // Gán vào biến class (nếu cần)
    }

    private void initMiniPlayer() {
        // Listener cho nút Play/Pause trên MiniPlayer
        miniPlayPause.setOnClickListener(v -> {
            MusicService.togglePlayPause();
            updateMiniPlayerPlayPauseButtonState(); // Chỉ cập nhật trạng thái nút Play/Pause
        });

        // Listener khi nhấp vào MiniPlayer để mở SongActivity
        miniPlayerContainer.setOnClickListener(v -> {
            Song currentServiceSong = MusicService.getCurrentSong();
            ArrayList<Song> currentServicePlaylist = MusicService.getCurrentPlaylist();
            int currentServiceIndex = MusicService.getCurrentIndexInPlaylist();

            if (currentServiceSong != null && currentServicePlaylist != null && currentServiceIndex != -1) {
                Intent intent = new Intent(MainActivity.this, SongActivity.class);
                intent.putExtra("SONG_LIST", currentServicePlaylist);
                intent.putExtra("SELECTED_INDEX", currentServiceIndex);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Chưa có bài hát nào được chọn.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMiniPlayerState() {
        if (miniPlayerContainer == null || tabLayout == null) {
            // Nếu các view chưa được khởi tạo (ví dụ: gọi quá sớm), thì không làm gì cả
            Log.w(TAG, "updateMiniPlayerState: Views chưa được khởi tạo.");
            return;
        }

        int currentTabPosition = tabLayout.getSelectedTabPosition();
        Song song = MusicService.getCurrentSong();

        // Log.d(TAG, "updateMiniPlayerState: Current Tab Position = " + currentTabPosition);
        // Log.d(TAG, "updateMiniPlayerState: Current Song = " + (song != null ? song.getTenBaiHat() : "null"));


        if (currentTabPosition == 0) { // Tab "Khám phá" (vị trí 0)
            miniPlayerContainer.setVisibility(View.GONE);
            // Log.d(TAG, "updateMiniPlayerState: Hiding miniPlayer for Khám Phá tab.");
        } else { // Các tab khác
            if (song != null) {
                miniPlayerContainer.setVisibility(View.VISIBLE);
                if (miniTitle != null) miniTitle.setText(song.getTenBaiHat());
                updateMiniPlayerPlayPauseButtonState();
                // Log.d(TAG, "updateMiniPlayerState: Showing miniPlayer for other tabs, song: " + song.getTenBaiHat());
            } else {
                miniPlayerContainer.setVisibility(View.GONE);
                // Log.d(TAG, "updateMiniPlayerState: Hiding miniPlayer for other tabs, no song.");
            }
        }
    }

    private void updateMiniPlayerPlayPauseButtonState() {
        if (miniPlayPause == null) return;
        if (MusicService.isPlaying()) {
            miniPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            miniPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void initTabs() {
        viewPager.setUserInputEnabled(false); // Vô hiệu hóa vuốt chuyển tab

        MainViewPageAdapter adapter = new MainViewPageAdapter(this);
        adapter.addFragment(new fragment_kham_pha(), "Khám phá"); // Position 0
        adapter.addFragment(new fragment_trang_chu(), "Trang chủ");  // Position 1
        adapter.addFragment(new fragment_ca_nhan(), "Cá nhân");    // Position 2

        viewPager.setAdapter(adapter);

        // Sử dụng TabLayoutMediator để thiết lập tiêu đề và icon cho tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getTitle(position));
            switch (position) {
                case 0: // Khám phá
                    tab.setIcon(R.drawable.icon_danhchoban);
                    break;
                case 1: // Trang chủ
                    tab.setIcon(R.drawable.home);
                    break;
                case 2: // Cá nhân
                    tab.setIcon(R.drawable.user);
                    break;
            }
        }).attach();

        // Thêm listener để theo dõi sự kiện chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab selected: " + tab.getPosition() + " - " + tab.getText());
                updateMiniPlayerState(); // Cập nhật trạng thái miniPlayer khi tab thay đổi
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần hành động cụ thể ở đây cho yêu cầu này
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nếu người dùng chọn lại tab "Khám phá", miniPlayer vẫn nên ẩn
                Log.d(TAG, "Tab reselected: " + tab.getPosition() + " - " + tab.getText());
                updateMiniPlayerState(); // Đảm bảo trạng thái đúng khi chọn lại
            }
        });

        // Đảm bảo viewPager hiển thị tab "Trang chủ" (vị trí 1) làm tab mặc định nếu muốn
        // viewPager.setCurrentItem(1, false); // false để không có animation
        // Nếu bạn muốn "Khám phá" là tab mặc định (vị trí 0), thì không cần dòng trên.
        // Và onTabSelected sẽ được gọi cho tab mặc định khi listener được attach.
    }
}