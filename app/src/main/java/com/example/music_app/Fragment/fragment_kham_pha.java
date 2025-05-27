package com.example.music_app.Fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.music_app.Activity.SongActivity; // Có thể không cần nếu không mở trực tiếp
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient; // Import ApiClient
import com.example.music_app.Service.APIService;   // Import ApiService
import com.example.music_app.Service.MusicService;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_kham_pha extends Fragment {

    private static final String TAG = "KhamPhaFragment";

    private ImageView imageSongBanner;
    private TextView songTitleHeader, songTitleMain, artistNameMain;
    private Button btnLyrics, btnFollow; // btnFollow chưa có chức năng cụ thể trong yêu cầu
    private ImageButton btnPlayFragment, iconFavorite, iconComment, iconShare, iconDownload, iconPlaylist; // Đổi tên btnPlay để tránh nhầm lẫn
    private SeekBar seekBarProgress;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    private Song currentRandomSong;
    private APIService apiService;

    public fragment_kham_pha() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = APIRetrofitClient.getClient().create(APIService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kham_pha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI(view);
        setupClickListeners();

        // Không gọi fetchAndPlayRandomSong() ở đây nữa, sẽ gọi trong onResume
        // để đảm bảo nó được gọi mỗi khi tab này hiển thị và phù hợp với logic ẩn/hiện mini player của MainActivity
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tab Khám Phá được hiển thị.");
        // Logic để quyết định có nên fetch bài mới hay không:
        // 1. Nếu chưa có currentRandomSong nào được tải.
        // 2. Hoặc nếu bài hát đang phát trong MusicService không phải là currentRandomSong.
        // Điều này cho phép người dùng quay lại tab và tiếp tục nghe nếu họ không làm gì khác.
        // Nếu muốn luôn tải bài mới khi vào tab, chỉ cần gọi fetchAndPlayRandomSong() trực tiếp.

        Song serviceCurrentSong = MusicService.getCurrentSong();
        ArrayList<Song> servicePlaylist = MusicService.getCurrentPlaylist();

        boolean shouldFetchNew = true;
        if (currentRandomSong != null && serviceCurrentSong != null && servicePlaylist != null && servicePlaylist.size() == 1) {
            if (serviceCurrentSong.get_id().equals(currentRandomSong.get_id()) &&
                    servicePlaylist.get(0).get_id().equals(currentRandomSong.get_id())) {
                // Đang phát đúng bài random song trước đó từ tab này
                Log.d(TAG, "onResume: Tiếp tục phát bài random cũ: " + currentRandomSong.getTenBaiHat());
                shouldFetchNew = false;
                updateUIWithSong(currentRandomSong); // Đảm bảo UI đúng
                initializePlayerControlsAndUpdater(); // Khởi tạo lại controls và updater
            }
        }

        if (shouldFetchNew) {
            Log.d(TAG, "onResume: Tải bài hát ngẫu nhiên mới.");
            fetchAndPlayRandomSong();
        }
        // MainActivity sẽ tự động ẩn miniPlayer của nó khi tab này được chọn
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Tab Khám Phá bị tạm dừng.");
        stopSeekBarUpdater();
        // Không dừng nhạc ở đây vì MusicService sẽ tiếp tục phát nền.
        // MainActivity sẽ hiển thị lại miniPlayer nếu người dùng chuyển sang tab khác.
    }

    private void initializeUI(View view) {
        imageSongBanner = view.findViewById(R.id.image_song_banner);
        songTitleHeader = view.findViewById(R.id.song_title_header);
        btnLyrics = view.findViewById(R.id.btn_lyrics);
        songTitleMain = view.findViewById(R.id.song_title_main);
        artistNameMain = view.findViewById(R.id.artist_name); // Đã có trong XML là artist_name, không phải artist_name_main
        // btnFollow = view.findViewById(R.id.btn_follow); // Xử lý nếu cần

        // Các icon điều khiển dưới cùng
//        iconFavorite = view.findViewById(R.id.icon_favorite);
//        iconComment = view.findViewById(R.id.icon_comment);
//        iconShare = view.findViewById(R.id.icon_share);
//        iconDownload = view.findViewById(R.id.icon_download);
//        iconPlaylist = view.findViewById(R.id.icon_playlist);

        // Điều khiển player chính trong fragment
        btnPlayFragment = view.findViewById(R.id.btnPlay); // ID trong XML của bạn là btnPlay
        seekBarProgress = view.findViewById(R.id.seekbar_progress);
    }

    private void setupClickListeners() {
        btnPlayFragment.setOnClickListener(v -> {
            if (currentRandomSong != null) { // Chỉ hoạt động nếu đã có bài hát
                MusicService.togglePlayPause();
                updatePlayPauseButtonIcon();
            } else {
                // Nếu chưa có bài hát, có thể thử tải lại
                fetchAndPlayRandomSong();
            }
        });

        btnLyrics.setOnClickListener(v -> {
            if (currentRandomSong != null && currentRandomSong.getLoiBaiHat() != null) {
                showLyrics(currentRandomSong.getLoiBaiHat());
            } else {
                Toast.makeText(getContext(), "Không có lời bài hát cho bài này.", Toast.LENGTH_SHORT).show();
            }
        });

        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && MusicService.getMediaPlayer() != null) {
                    MusicService.getMediaPlayer().seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { stopSeekBarUpdater(); }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { startSeekBarUpdater(); }
        });

        // Thêm listener cho các nút khác nếu cần (favorite, comment, share, download, playlist)
        // Ví dụ:
        // iconFavorite.setOnClickListener(v -> Toast.makeText(getContext(), "Favorite clicked", Toast.LENGTH_SHORT).show());
    }


    private void fetchAndPlayRandomSong() {
        Log.d(TAG, "Đang tải bài hát ngẫu nhiên...");
        // Hiển thị trạng thái loading nếu cần
        apiService.getRandomSong().enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRandomSong = response.body();
                    Log.d(TAG, "Tải thành công bài hát ngẫu nhiên: " + currentRandomSong.getTenBaiHat());

                    ArrayList<Song> playlist = new ArrayList<>();
                    playlist.add(currentRandomSong);

                    // Phát nhạc
                    if (getContext() != null) {
                        MusicService.play(getContext(), currentRandomSong, playlist, 0);
                    }

                    updateUIWithSong(currentRandomSong);
                    initializePlayerControlsAndUpdater(); // Khởi tạo lại controls và updater
                } else {
                    Log.e(TAG, "Lỗi khi tải bài hát ngẫu nhiên: " + response.message());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải bài hát ngẫu nhiên.", Toast.LENGTH_SHORT).show();
                    }
                    // Reset UI nếu không tải được
                    updateUIWithSong(null);
                }
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                Log.e(TAG, "Lỗi API khi tải bài hát ngẫu nhiên: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối khi tải bài hát.", Toast.LENGTH_SHORT).show();
                }
                updateUIWithSong(null);
            }
        });
    }

    private void updateUIWithSong(Song song) {
        if (song != null) {
            if (songTitleHeader != null) songTitleHeader.setText(song.getTenBaiHat() + " - " + song.getCaSi()); // Ví dụ
            if (imageSongBanner != null && getContext() != null) {
                Glide.with(getContext()).load(song.getHinhBaiHat()).placeholder(R.drawable.default_song_image).into(imageSongBanner);
            }
            if (songTitleMain != null) songTitleMain.setText(song.getTenBaiHat());
            if (artistNameMain != null) artistNameMain.setText(song.getCaSi()); // Sửa id thành artist_name nếu nó là id trong XML
            // Cập nhật các TextView tiếng Trung nếu cần
        } else {
            // Reset UI về trạng thái mặc định nếu không có bài hát
            if (songTitleHeader != null) songTitleHeader.setText("Đang tải...");
            if (imageSongBanner != null) imageSongBanner.setImageResource(R.drawable.default_song_image); // Ảnh mặc định
            if (songTitleMain != null) songTitleMain.setText("Chưa có bài hát");
            if (artistNameMain != null) artistNameMain.setText("");
            if(seekBarProgress != null) seekBarProgress.setProgress(0);
            if(seekBarProgress != null) seekBarProgress.setMax(0); // Quan trọng để reset seekbar
            updatePlayPauseButtonIcon(); // Cập nhật nút play/pause về trạng thái mặc định
        }
    }

    private void initializePlayerControlsAndUpdater() {
        updatePlayPauseButtonIcon(); // Cập nhật icon play/pause
        setFragmentSpecificCompletionListener(); // Thiết lập listener khi bài hát hoàn thành CHO FRAGMENT NÀY
        startSeekBarUpdater(); // Bắt đầu cập nhật SeekBar
    }


    private void updatePlayPauseButtonIcon() {
        if(btnPlayFragment == null) return;
        // Nút play/pause này chỉ nên phản ánh trạng thái của currentRandomSong
        // nếu nó đang là bài hát trong MusicService
        Song serviceSong = MusicService.getCurrentSong();
        if (currentRandomSong != null && serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id()) && MusicService.isPlaying()) {
            btnPlayFragment.setImageResource(R.drawable.ic_pause); // Sử dụng icon pause lớn hơn nếu có
        } else {
            btnPlayFragment.setImageResource(R.drawable.ic_play); // Sử dụng icon play lớn hơn nếu có
        }
    }

    private void startSeekBarUpdater() {
        stopSeekBarUpdater(); // Dừng updater cũ nếu có

        MediaPlayer player = MusicService.getMediaPlayer();
        if (player != null && currentRandomSong != null) {
            try {
                // Chỉ cập nhật seekbar nếu bài hát đang phát là currentRandomSong
                Song serviceSong = MusicService.getCurrentSong();
                if (serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
                    seekBarProgress.setMax(player.getDuration());
                    updateSeekBarRunnable = new Runnable() {
                        @Override
                        public void run() {
                            MediaPlayer currentMediaPlayer = MusicService.getMediaPlayer();
                            Song currentServiceSong = MusicService.getCurrentSong();
                            // Kiểm tra lại một lần nữa trước khi cập nhật
                            if (currentMediaPlayer != null && currentRandomSong != null &&
                                    currentServiceSong != null && currentServiceSong.get_id().equals(currentRandomSong.get_id()) &&
                                    currentMediaPlayer.isPlaying()) {
                                try {
                                    seekBarProgress.setProgress(currentMediaPlayer.getCurrentPosition());
                                    handler.postDelayed(this, 500);
                                } catch (IllegalStateException e) {
                                    Log.w(TAG, "SeekBarUpdater: IllegalStateException, player có thể đã thay đổi.");
                                    stopSeekBarUpdater();
                                }
                            } else {
                                // Dừng cập nhật nếu điều kiện không còn đúng
                                stopSeekBarUpdater();
                                // Có thể reset seekbar ở đây nếu bài hát đã thay đổi hoặc dừng
                                // seekBarProgress.setProgress(0);
                                updatePlayPauseButtonIcon(); // Cập nhật lại nút play/pause
                            }
                        }
                    };
                    handler.post(updateSeekBarRunnable);
                } else {
                    Log.d(TAG, "startSeekBarUpdater: Bài hát trong service không phải currentRandomSong, không cập nhật seekbar.");
                    seekBarProgress.setMax(0);
                    seekBarProgress.setProgress(0);
                    updatePlayPauseButtonIcon();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "startSeekBarUpdater: IllegalStateException khi lấy duration.", e);
                seekBarProgress.setMax(0);
                seekBarProgress.setProgress(0);
            }
        } else {
            seekBarProgress.setMax(0);
            seekBarProgress.setProgress(0);
            updatePlayPauseButtonIcon(); // Đảm bảo nút play đúng trạng thái
        }
    }

    private void stopSeekBarUpdater() {
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void showLyrics(String lrcString) {
        if (lrcString == null || lrcString.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy lời bài hát.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getActivity() != null && MusicService.getMediaPlayer() != null) {
            LyricsBottomSheetFragment lyricsFragment = new LyricsBottomSheetFragment();
            Bundle args = new Bundle();
            args.putString("LRC_STRING", lrcString);
            lyricsFragment.setArguments(args);
            // Fragment lời bài hát sẽ tự lấy MediaPlayer từ MusicService nếu cần
            // hoặc bạn có thể truyền mediaPlayer nếu thiết kế của bạn yêu cầu
            // lyricsFragment.setMediaPlayer(MusicService.getMediaPlayer());
            lyricsFragment.show(getActivity().getSupportFragmentManager(), lyricsFragment.getTag());
        }
    }

    // Listener này dành riêng cho khi bài hát ngẫu nhiên trong fragment này kết thúc
    private void setFragmentSpecificCompletionListener() {
        MediaPlayer player = MusicService.getMediaPlayer();
        Song serviceSong = MusicService.getCurrentSong();

        // Chỉ đặt listener nếu player tồn tại và bài hát hiện tại của service là bài random này
        if (player != null && currentRandomSong != null && serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
            Log.d(TAG, "Thiết lập OnCompletionListener cho fragment Khám Phá, bài: " + currentRandomSong.getTenBaiHat());
            player.setOnCompletionListener(mp -> {
                Log.d(TAG, "Bài hát ngẫu nhiên '" + (currentRandomSong != null ? currentRandomSong.getTenBaiHat() : "") + "' đã kết thúc trong fragment Khám Phá.");
                // Cập nhật UI: nút play, seekbar về cuối
                if(getContext() != null) { // Kiểm tra context trước khi dùng
                    updatePlayPauseButtonIcon(); // Chuyển về icon play
                    if (seekBarProgress != null && mp != null) { // Kiểm tra null cho mp
                        try {
                            seekBarProgress.setProgress(mp.getDuration()); // SeekBar về cuối
                        } catch (IllegalStateException e) {
                            seekBarProgress.setProgress(seekBarProgress.getMax());
                        }
                    }
                    // Tùy chọn: Tự động tải và phát một bài hát ngẫu nhiên khác
                    // fetchAndPlayRandomSong();
                    // Hoặc chỉ dừng lại
                }
            });
        } else {
            // Nếu không phải bài hát của fragment này, không ghi đè listener
            // Hoặc đảm bảo listener của SongActivity được đặt lại nếu cần khi chuyển tab
            Log.d(TAG, "Không thiết lập OnCompletionListener của fragment vì bài hát không khớp hoặc player null.");
        }
    }
}