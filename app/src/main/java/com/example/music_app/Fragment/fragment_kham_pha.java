package com.example.music_app.Fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector; // Thêm import này
import android.view.LayoutInflater;
import android.view.MotionEvent; // Thêm import này
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat; // Thêm import này
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;
import com.example.music_app.Service.MusicService;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_kham_pha extends Fragment {

    private static final String TAG = "KhamPhaFragment";
    private boolean autoPlayNextRandom = true;
    // ... (các biến UI đã có) ...
    private ImageView imageSongBanner;
    private TextView songTitleHeader, songTitleMain, artistNameMain;
    private TextView textVerticalChinese, textArtistBanner;
    private Button btnLyrics;
    private ImageButton btnPlayFragment;
    private SeekBar seekBarProgress;
    private ProgressBar loadingProgressBar;
    private View mainContentGroup;


    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    private Song currentRandomSong; // Bài hát ngẫu nhiên hiện tại đang hiển thị/phát
    private APIService apiService;
    private boolean isFetching = false;

    // Cho lịch sử bài hát và cử chỉ vuốt
    private ArrayList<Song> songHistory = new ArrayList<>();
    private int currentHistoryIndex = -1;
    private GestureDetectorCompat gestureDetector;


    public fragment_kham_pha() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = APIRetrofitClient.getClient().create(APIService.class);
        // Khởi tạo GestureDetector
        if (getContext() != null) {
            gestureDetector = new GestureDetectorCompat(getContext(), new SwipeGestureListener());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kham_pha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI(view);
        setupClickListeners();

        // Thiết lập listener cho cử chỉ chạm trên view gốc của fragment
        // Điều này cho phép phát hiện vuốt trên toàn bộ fragment
        view.setOnTouchListener((v, event) -> {
            if (gestureDetector != null) {
                return gestureDetector.onTouchEvent(event);
            }
            return false; // Hoặc v.performClick() nếu cần thiết cho accessibility
        });

        // Trạng thái loading ban đầu nếu chưa có lịch sử
        if (songHistory.isEmpty() && currentRandomSong == null) {
            showLoading(true);
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (mainContentGroup != null) {
            mainContentGroup.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tab Khám Phá. History size: " + songHistory.size() + ", CurrentIndex: " + currentHistoryIndex);

        Song songToDisplayFromHistory = null;
        if (currentHistoryIndex >= 0 && currentHistoryIndex < songHistory.size()) {
            songToDisplayFromHistory = songHistory.get(currentHistoryIndex);
        }

        Song serviceCurrentSong = MusicService.getCurrentSong();
        ArrayList<Song> servicePlaylist = MusicService.getCurrentPlaylist();

        boolean serviceIsPlayingThisFragmentSpecificSong = false;
        if (songToDisplayFromHistory != null && serviceCurrentSong != null &&
                servicePlaylist != null && servicePlaylist.size() == 1 && // Playlist của service là cho bài đơn lẻ
                serviceCurrentSong.get_id().equals(songToDisplayFromHistory.get_id()) &&
                servicePlaylist.get(0).get_id().equals(songToDisplayFromHistory.get_id())) {
            serviceIsPlayingThisFragmentSpecificSong = true;
        }

        if (serviceIsPlayingThisFragmentSpecificSong) {
            // Service đang phát đúng bài hát mà fragment này đang quản lý từ lịch sử
            this.currentRandomSong = songToDisplayFromHistory; // Đồng bộ lại
            Log.d(TAG, "onResume: Service đang phát bài từ lịch sử: " + currentRandomSong.getTenBaiHat());
            showLoading(false);
            updateUIWithSong(currentRandomSong);
            initializePlayerControlsAndUpdater();
        } else if (songToDisplayFromHistory != null) {
            // Có bài trong lịch sử tại index hiện tại, nhưng service không phát nó (hoặc phát bài khác)
            // -> Phát bài này từ lịch sử
            Log.d(TAG, "onResume: Phát bài từ lịch sử (service không phát bài này): " + songToDisplayFromHistory.getTenBaiHat());
            showLoading(false); // Hoặc true nếu playSpecificSong có thể bất đồng bộ
            playSpecificSong(songToDisplayFromHistory);
        } else {
            // Không có bài hát hợp lệ trong lịch sử tại currentHistoryIndex (ví dụ: lần đầu mở app)
            Log.d(TAG, "onResume: Lịch sử rỗng hoặc index không hợp lệ. Tải bài mới.");
            if (!isFetching) {
                showLoading(true);
                fetchAndPlayRandomSong();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Tab Khám Phá bị tạm dừng.");
        stopSeekBarUpdater();
    }

    private void initializeUI(View view) {
        // ... (giữ nguyên phần findViewById) ...
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        mainContentGroup = view.findViewById(R.id.mainContentGroup);
        imageSongBanner = view.findViewById(R.id.image_song_banner);
        songTitleHeader = view.findViewById(R.id.song_title_header);
        btnLyrics = view.findViewById(R.id.btn_lyrics);
        songTitleMain = view.findViewById(R.id.song_title_main);
        artistNameMain = view.findViewById(R.id.artist_name);
        btnPlayFragment = view.findViewById(R.id.btnPlay);
        seekBarProgress = view.findViewById(R.id.seekbar_progress);
    }

    private void setupClickListeners() {
        // ... (giữ nguyên listeners cho btnPlayFragment, btnLyrics, seekBarProgress) ...
        btnPlayFragment.setOnClickListener(v -> {
            if (currentRandomSong != null) {
                MusicService.togglePlayPause();
                updatePlayPauseButtonIcon();
            } else if (!isFetching) {
                showLoading(true);
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
                if (fromUser && MusicService.getMediaPlayer() != null && currentRandomSong != null) {
                    Song serviceSong = MusicService.getCurrentSong();
                    if (serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
                        try {
                            MusicService.getMediaPlayer().seekTo(progress);
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Seekbar onProgressChanged: IllegalStateException", e);
                        }
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (currentRandomSong != null) {
                    Song serviceSong = MusicService.getCurrentSong();
                    if (serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
                        stopSeekBarUpdater();
                    }
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentRandomSong != null) {
                    Song serviceSong = MusicService.getCurrentSong();
                    if (serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
                        startSeekBarUpdater();
                    }
                }
            }
        });
    }


    private void fetchAndPlayRandomSong() {
        if (isFetching) {
            Log.d(TAG, "Đang trong quá trình tải bài hát, vui lòng đợi...");
            return;
        }
        isFetching = true;
        // showLoading(true) đã được gọi ở nơi gọi hàm này (onResume hoặc onSwipeUp)

        Log.d(TAG, "Bắt đầu tải bài hát ngẫu nhiên...");
        apiService.getRandomSong().enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                isFetching = false;
                if (getContext() == null || !isAdded()) return;

                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Song fetchedSong = response.body();
                    Log.d(TAG, "Tải thành công bài hát ngẫu nhiên: " + fetchedSong.getTenBaiHat());

                    // Quản lý lịch sử
                    // Nếu đang ở giữa lịch sử và fetch bài mới, các bài "tương lai" sẽ bị xóa
                    if (currentHistoryIndex < songHistory.size() - 1) {
                        songHistory = new ArrayList<>(songHistory.subList(0, currentHistoryIndex + 1));
                    }
                    songHistory.add(fetchedSong);
                    currentHistoryIndex = songHistory.size() - 1;
                    Log.d(TAG, "Bài hát mới được thêm vào lịch sử. Size: " + songHistory.size() + ", Index: " + currentHistoryIndex);

                    playSpecificSong(fetchedSong); // Phát bài hát mới fetch
                } else {
                    Log.e(TAG, "Lỗi khi tải bài hát ngẫu nhiên: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Không thể tải bài hát ngẫu nhiên.", Toast.LENGTH_SHORT).show();
                    updateUIWithSong(null); // Có thể hiển thị UI lỗi ở đây
                }
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                isFetching = false;
                if (getContext() == null || !isAdded()) return;

                showLoading(false);
                Log.e(TAG, "Lỗi API khi tải bài hát ngẫu nhiên: ", t);
                Toast.makeText(getContext(), "Lỗi kết nối hoặc API.", Toast.LENGTH_SHORT).show();
                updateUIWithSong(null); // Có thể hiển thị UI lỗi
            }
        });
    }

    private void playSpecificSong(Song songToPlay) {
        if (getContext() == null || songToPlay == null || !isAdded()) {
            Log.w(TAG, "playSpecificSong: Context null, song null hoặc fragment not added.");
            showLoading(false); // Ẩn loading nếu có lỗi xảy ra ở đây
            return;
        }

        this.currentRandomSong = songToPlay; // Cập nhật bài hát hiện tại của fragment

        ArrayList<Song> playlist = new ArrayList<>();
        playlist.add(songToPlay);

        // MusicService sẽ lưu playlist này và index 0
        MusicService.play(getContext(), songToPlay, playlist, 0);

        updateUIWithSong(songToPlay);
        initializePlayerControlsAndUpdater(); // Khởi tạo lại controls, updater, và listener
    }


    private void updateUIWithSong(Song song) {
        // ... (giữ nguyên như trước, đảm bảo kiểm tra isAdded() và getContext() != null) ...
        if (!isAdded() || getContext() == null) return;

        if (song != null) {
            if (songTitleHeader != null) songTitleHeader.setText(song.getTenBaiHat() + (song.getCaSi() != null ? " - " + song.getCaSi() : ""));
            if (imageSongBanner != null) {
                Glide.with(getContext()).load(song.getHinhBaiHat()).placeholder(R.drawable.default_song_image).error(R.drawable.default_song_image).into(imageSongBanner);
            }
            if (songTitleMain != null) songTitleMain.setText(song.getTenBaiHat());
            if (artistNameMain != null) artistNameMain.setText(song.getCaSi());
            // Cập nhật các TextView tiếng Trung nếu có trường dữ liệu
            // if (textVerticalChinese != null && song.getChineseTitle() != null) textVerticalChinese.setText(song.getChineseTitle());
            // if (textArtistBanner != null && song.getChineseArtist() != null) textArtistBanner.setText(song.getChineseArtist());

        } else {
            if (songTitleHeader != null) songTitleHeader.setText("Không có bài hát");
            if (imageSongBanner != null) imageSongBanner.setImageResource(R.drawable.default_song_image);
            if (songTitleMain != null) songTitleMain.setText("N/A");
            if (artistNameMain != null) artistNameMain.setText("");
            // if (textVerticalChinese != null) textVerticalChinese.setText("");
            // if (textArtistBanner != null) textArtistBanner.setText("");
            if(seekBarProgress != null) seekBarProgress.setProgress(0);
            if(seekBarProgress != null) seekBarProgress.setMax(0);
            updatePlayPauseButtonIcon();
        }
    }

    private void initializePlayerControlsAndUpdater() {
        // ... (giữ nguyên như trước, đảm bảo kiểm tra isAdded()) ...
        if (!isAdded()) return;
        updatePlayPauseButtonIcon();
        setFragmentSpecificCompletionListener();
        startSeekBarUpdater();
    }


    private void updatePlayPauseButtonIcon() {
        // ... (giữ nguyên như trước, đảm bảo kiểm tra isAdded()) ...
        if(btnPlayFragment == null || !isAdded()) return;
        Song serviceSong = MusicService.getCurrentSong();
        if (currentRandomSong != null && serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id()) && MusicService.isPlaying()) {
            btnPlayFragment.setImageResource(R.drawable.ic_pause);
        } else {
            btnPlayFragment.setImageResource(R.drawable.ic_play);
        }
    }

    private void startSeekBarUpdater() {
        // ... (giữ nguyên như trước, đảm bảo kiểm tra isAdded()) ...
        if (!isAdded()) return;
        stopSeekBarUpdater();

        MediaPlayer player = MusicService.getMediaPlayer();
        Song serviceSong = MusicService.getCurrentSong();
        if (player != null && currentRandomSong != null && serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
            try {
                int duration = player.getDuration();
                if (duration <= 0) {
                    Log.w(TAG, "startSeekBarUpdater: Duration không hợp lệ (" + duration + ").");
                    resetSeekBarProgressUI();
                    // Không thử lại bằng handler ở đây nữa, vì có thể gây vòng lặp nếu player lỗi thật
                    return;
                }
                seekBarProgress.setMax(duration);
                updateSeekBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) return;
                        MediaPlayer currentMediaPlayer = MusicService.getMediaPlayer();
                        Song currentServiceSongInner = MusicService.getCurrentSong();
                        if (currentMediaPlayer != null && currentRandomSong != null &&
                                currentServiceSongInner != null && currentServiceSongInner.get_id().equals(currentRandomSong.get_id())) {
                            try {
                                if (currentMediaPlayer.isPlaying()) {
                                    seekBarProgress.setProgress(currentMediaPlayer.getCurrentPosition());
                                }
                                handler.postDelayed(this, 500);
                            } catch (IllegalStateException e) {
                                Log.w(TAG, "SeekBarUpdater: IllegalStateException", e);
                                stopSeekBarUpdater();
                                resetSeekBarProgressUI();
                                updatePlayPauseButtonIcon();
                            }
                        } else {
                            stopSeekBarUpdater();
                            resetSeekBarProgressUI();
                            updatePlayPauseButtonIcon();
                        }
                    }
                };
                handler.post(updateSeekBarRunnable);
            } catch (IllegalStateException e) {
                Log.e(TAG, "startSeekBarUpdater: IllegalStateException khi lấy duration.", e);
                resetSeekBarProgressUI();
            }
        } else {
            Log.d(TAG, "startSeekBarUpdater: Player null hoặc bài hát không khớp, không bắt đầu updater.");
            resetSeekBarProgressUI();
            updatePlayPauseButtonIcon();
        }
    }
    private void resetSeekBarProgressUI() {
        // ... (giữ nguyên như trước) ...
        if (seekBarProgress != null) {
            seekBarProgress.setMax(0);
            seekBarProgress.setProgress(0);
        }
    }


    private void stopSeekBarUpdater() {
        // ... (giữ nguyên như trước) ...
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void showLyrics(String lrcString) {
        // ... (giữ nguyên như trước, đảm bảo kiểm tra isAdded() và getActivity()) ...
        if (!isAdded() || getActivity() == null) return;
        if (lrcString == null || lrcString.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy lời bài hát.", Toast.LENGTH_SHORT).show();
            return;
        }
        LyricsBottomSheetFragment lyricsFragment = new LyricsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("LRC_STRING", lrcString);
        lyricsFragment.setArguments(args);
        lyricsFragment.show(getChildFragmentManager(), lyricsFragment.getTag());
    }

    private void setFragmentSpecificCompletionListener() {
        if (!isAdded()) return; // Đảm bảo fragment vẫn còn attached
        MediaPlayer player = MusicService.getMediaPlayer();
        Song serviceSong = MusicService.getCurrentSong();

        // Chỉ đặt listener nếu player tồn tại và bài hát hiện tại của service là bài random này
        if (player != null && currentRandomSong != null && serviceSong != null && serviceSong.get_id().equals(currentRandomSong.get_id())) {
            Log.d(TAG, "Thiết lập OnCompletionListener cho fragment Khám Phá, bài: " + currentRandomSong.getTenBaiHat());

            player.setOnCompletionListener(mp -> {
                if (!isAdded() || getContext() == null) return; // Kiểm tra lại trước khi thực hiện hành động

                Log.d(TAG, "Bài hát ngẫu nhiên '" + (currentRandomSong != null ? currentRandomSong.getTenBaiHat() : "[đã null]") + "' đã kết thúc trong fragment Khám Phá.");

                // Cập nhật UI cho bài hát vừa kết thúc
                updatePlayPauseButtonIcon(); // Nên chuyển về icon play
                if (seekBarProgress != null && mp != null) {
                    try {
                        int duration = mp.getDuration();
                        if (duration > 0) {
                            // Chỉ cập nhật nếu max của seekbar khớp với duration thực tế
                            if (seekBarProgress.getMax() == duration) {
                                seekBarProgress.setProgress(duration); // SeekBar về cuối
                            } else {
                                // Nếu max không khớp, có thể do lỗi trước đó, set về 0 hoặc max hiện tại
                                seekBarProgress.setProgress(seekBarProgress.getMax());
                            }
                        } else {
                            seekBarProgress.setProgress(0); // Nếu duration không hợp lệ
                        }
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "Lỗi khi lấy duration lúc onCompletion: " + e.getMessage());
                        if (seekBarProgress.getMax() > 0) {
                            seekBarProgress.setProgress(seekBarProgress.getMax()); // fallback
                        } else {
                            seekBarProgress.setProgress(0);
                        }
                    }
                }

                // Tự động tải và phát một bài hát ngẫu nhiên khác
                if (autoPlayNextRandom) { // Kiểm tra cờ nếu bạn muốn có thể tắt/bật tính năng này
                    if (!isFetching) {
                        Log.i(TAG, "OnCompletion: Tự động tải bài hát ngẫu nhiên mới.");
                        showLoading(true); // Hiển thị loading cho bài hát tiếp theo
                        fetchAndPlayRandomSong(); // Gọi API để lấy và phát bài mới
                    } else {
                        Log.d(TAG, "OnCompletion: Đang trong quá trình fetch, không gọi fetchAndPlayRandomSong() mới.");
                    }
                } else {
                    Log.d(TAG, "OnCompletion: autoPlayNextRandom đang là false, không tự động phát bài mới.");
                }
            });
        } else {
            // Điều này bình thường nếu người dùng đã chuyển sang một bài hát khác không phải do fragment này quản lý
            // hoặc khi fragment vừa được tạo và currentRandomSong chưa được thiết lập.
            Log.d(TAG, "Không thiết lập OnCompletionListener của fragment vì bài hát không khớp, player null, hoặc currentRandomSong null.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSeekBarUpdater();
    }

    // Lớp nội bộ để xử lý cử chỉ vuốt
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100; // Ngưỡng khoảng cách vuốt
        private static final int SWIPE_VELOCITY_THRESHOLD = 100; // Ngưỡng vận tốc vuốt

        @Override
        public boolean onDown(MotionEvent e) {
            // Cần trả về true để onFling hoạt động
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            // Ưu tiên vuốt dọc
            if (Math.abs(diffY) > Math.abs(diffX)) {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // Vuốt xuống (e2.getY() > e1.getY())
                        handleSwipeDown();
                    } else {
                        // Vuốt lên (e2.getY() < e1.getY())
                        handleSwipeUp();
                    }
                    return true; // Đã xử lý cử chỉ
                }
            }
            return false; // Không phải cử chỉ vuốt dọc đủ mạnh
        }
    }

    private void handleSwipeUp() {
        Log.d(TAG, "Cử chỉ VUỐT LÊN được phát hiện.");
        Toast.makeText(getContext(), "Đang tải bài hát ngẫu nhiên mới...", Toast.LENGTH_SHORT).show();
        if (!isFetching) {
            showLoading(true);
            fetchAndPlayRandomSong(); // Gọi API để lấy bài mới và cập nhật lịch sử
        }
    }

    private void handleSwipeDown() {
        Log.d(TAG, "Cử chỉ VUỐT XUỐNG được phát hiện.");
        if (isFetching) {
            Log.d(TAG, "Đang tải bài mới, bỏ qua vuốt xuống.");
            return;
        }
        if (currentHistoryIndex > 0) { // Phải có ít nhất một bài trước đó trong lịch sử
            currentHistoryIndex--;
            Song previousSong = songHistory.get(currentHistoryIndex);
            Log.d(TAG, "Phát lại bài hát trước đó từ lịch sử: " + previousSong.getTenBaiHat());
            Toast.makeText(getContext(), "Đang phát lại: " + previousSong.getTenBaiHat(), Toast.LENGTH_SHORT).show();
            showLoading(false); // Không cần loading khi phát từ lịch sử (trừ khi có logic khác)
            playSpecificSong(previousSong);
        } else {
            Log.d(TAG, "Không có bài hát trước đó trong lịch sử hoặc đang ở bài đầu tiên.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Không có bài hát trước đó trong lịch sử.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}