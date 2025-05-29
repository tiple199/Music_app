package com.example.music_app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.music_app.Fragment.LyricsBottomSheetFragment;
import com.example.music_app.Model.Favorite;
import com.example.music_app.Model.Playlist;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;
import com.example.music_app.Service.MusicService;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongActivity extends AppCompatActivity {
    private static final String TAG = "SongActivity";

    private enum RepeatMode {
        REPEAT_ALL,
        REPEAT_ONE
    }
    private RepeatMode repeatMode = RepeatMode.REPEAT_ALL;

    private ArrayList<Song> songList;
    private Song currentSong;
    private int currentTrackIndex = 0;

    private ImageButton playButton, nextButton, previousButton, btnClose, repeatButton;
    private Button btnShowLyrics;
    private ImageView imageView;
    private TextView songName, artistName;

    private Handler handler;
    private Runnable updateSeekBarRunnable;
    private SeekBar seekBar;
    private TextView currentTimeText, totalTimeText;
    private boolean isFavorite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        if (!loadIntentData()) {
            return;
        }

        handler = new Handler(Looper.getMainLooper());
        initializeUI();

        currentSong = songList.get(currentTrackIndex);

        setupButtonClickListeners();


        // Phan xu ly khi nhan tym thi them bai hat vao danh sach ua thich
        ImageButton btnFavorite = findViewById(R.id.btnFavorite);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);

        Call<JsonObject> checkCall = apiService.isSongFavorited(userId, currentSong.get_id());
        checkCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean favorited = response.body().get("isFavorite").getAsBoolean();
                    isFavorite = favorited;
                    btnFavorite.setImageResource(favorited ? R.drawable.ic_favorited : R.drawable.ic_favorite);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("FavoriteCheck", "Lỗi khi kiểm tra yêu thích: " + t.getMessage());
            }
        });
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorited : R.drawable.ic_favorite);

        btnFavorite.setOnClickListener(v -> {

            if (userId == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isFavorite) {
                //  Gọi API thêm yêu thích
                Call<Favorite> call = apiService.addFavorite(userId, currentSong.get_id());
                call.enqueue(new Callback<Favorite>() {
                    @Override
                    public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                        if (response.isSuccessful()) {
                            isFavorite = true;
                            btnFavorite.setImageResource(R.drawable.ic_favorited);
                            Toast.makeText(SongActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SongActivity.this, "Đã tồn tại trong yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Favorite> call, Throwable t) {
                        Toast.makeText(SongActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Gọi API xóa yêu thích
                Call<Void> call = apiService.removeFavorite(userId, currentSong.get_id());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful() || response.code() == 204) {
                            isFavorite = false;
                            btnFavorite.setImageResource(R.drawable.ic_favorite);
                            Toast.makeText(SongActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SongActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SongActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //

        // Xu ly khi nhan vao nut them playlist
        ImageButton btnFeature = findViewById(R.id.btn_addSongtoPlaylist);
        btnFeature.setOnClickListener(v -> showAddToPlaylistDialog());



        if (MusicService.getCurrentSong() == null ||
                !MusicService.getCurrentSong().get_id().equals(currentSong.get_id()) ||
                MusicService.getCurrentPlaylist() == null) { // Kiểm tra cả playlist
            // Nếu service không có bài hát, hoặc bài hát khác, hoặc không có playlist,
            // thì yêu cầu phát bài hát hiện tại của Activity cùng với context của nó.
            MusicService.play(this, currentSong, songList, currentTrackIndex);
        } else {
            // Service đã có bài hát và playlist khớp, không cần gọi play(),
            // nhưng đảm bảo UI của SongActivity đồng bộ với trạng thái của service.
            Log.d(TAG, "Service đã phát bài hát hiện tại. Đồng bộ UI.");
        }
        setCompletionListenerForCurrentPlayer();

        updateSongInfoUI();
        updatePlayPauseIcon();
        initializeSeekBarAndUpdater();
    }

    // Doan xu ly khi them bai hat vao playlist
    private void showAddToPlaylistDialog() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Playlist>> call = apiService.getUserPlaylists(userId);

        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Playlist> playlists = response.body();
                    showPlaylistDialog(playlists);
                } else {
                    Toast.makeText(SongActivity.this, "Không thể tải danh sách phát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Toast.makeText(SongActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistDialog(List<Playlist> playlists) {
        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = playlists.get(i).getTen(); // chú ý getter đúng tên
        }

        new AlertDialog.Builder(this)
                .setTitle("Thêm vào Playlist")
                .setItems(names, (dialog, which) -> {
                    String selectedPlaylistId = playlists.get(which).get_id(); // dùng playlistId
                    addCurrentSongToPlaylist(selectedPlaylistId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addCurrentSongToPlaylist(String playlistId) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<ResponseBody> call = apiService.addSongToPlaylist(playlistId, currentSong.get_id());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SongActivity.this, "Đã thêm vào playlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SongActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SongActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //



    private boolean loadIntentData() {
        Intent intent = getIntent();
        songList = (ArrayList<Song>) intent.getSerializableExtra("SONG_LIST");
        currentTrackIndex = intent.getIntExtra("SELECTED_INDEX", 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "Danh sách bài hát rỗng hoặc không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        // Đảm bảo currentTrackIndex nằm trong giới hạn hợp lệ
        if (currentTrackIndex < 0 || currentTrackIndex >= songList.size()) {
            Log.w(TAG, "SELECTED_INDEX không hợp lệ (" + currentTrackIndex + "), đặt lại về 0.");
            currentTrackIndex = 0;
        }
        return true;
    }

    private void initializeUI() {
        playButton = findViewById(R.id.playButton_test);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        btnClose = findViewById(R.id.btnClose);
        imageView = findViewById(R.id.image_song);
        songName = findViewById(R.id.name_song);
        artistName = findViewById(R.id.artist_name);
        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        btnShowLyrics = findViewById(R.id.btnShowLyrics);
        repeatButton = findViewById(R.id.repeatButton);
    }

    private void setupButtonClickListeners() {
        repeatButton.setOnClickListener(v -> toggleRepeatMode());

        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());

        btnShowLyrics.setOnClickListener(v -> showLyrics());
        playButton.setOnClickListener(v -> {
            MusicService.togglePlayPause();
            updatePlayPauseIcon();
        });
        btnClose.setOnClickListener(v -> {
            finish(); // Chỉ cần finish, MainActivity sẽ lấy lại context từ MusicService
            overridePendingTransition(0, R.anim.slide_out_down);
        });
    }

    private void toggleRepeatMode() {
        if (repeatMode == RepeatMode.REPEAT_ALL) {
            repeatMode = RepeatMode.REPEAT_ONE;
            repeatButton.setImageResource(R.drawable.repeat_one_icon);
            Toast.makeText(this, "Lặp lại một bài", Toast.LENGTH_SHORT).show();
        } else {
            repeatMode = RepeatMode.REPEAT_ALL;
            repeatButton.setImageResource(R.drawable.repeat_all_icon);
            Toast.makeText(this, "Lặp lại danh sách", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "RepeatMode changed to: " + repeatMode);
    }

    private void setCompletionListenerForCurrentPlayer() {
        MediaPlayer currentPlayer = MusicService.getMediaPlayer();
        if (currentPlayer != null) {
            Log.i(TAG, "Thiết lập OnCompletionListener cho player: " + currentPlayer.hashCode() + " của bài: " + (currentSong != null ? currentSong.getTenBaiHat() : "N/A"));
            currentPlayer.setOnCompletionListener(mp -> {
                Log.i(TAG, "OnCompletionListener: Bài hát đã hoàn thành (player: " + mp.hashCode() + "). Chế độ lặp: " + repeatMode);
                handleSongCompletion();
            });
        } else {
            Log.w(TAG, "setCompletionListenerForCurrentPlayer: MediaPlayer hiện tại là null.");
        }
    }

    private void handleSongCompletion() {
        if (currentSong == null || songList == null || songList.isEmpty()) {
            Log.e(TAG, "handleSongCompletion: currentSong hoặc songList là null/rỗng. Không thể xử lý.");
            return;
        }
        if (repeatMode == RepeatMode.REPEAT_ONE) {
            Log.i(TAG, "handleSongCompletion: REPEAT_ONE - Yêu cầu phát lại bài: " + currentSong.getTenBaiHat());
            MusicService.play(this, currentSong, songList, currentTrackIndex); // Truyền context
            updateAllUIAndListenersForNewSong(); // Cập nhật sau khi play
        } else if (repeatMode == RepeatMode.REPEAT_ALL) {
            Log.i(TAG, "handleSongCompletion: REPEAT_ALL - Chuyển bài tiếp theo.");
            playSongAtIndexLogic(true);
        } else {
            Log.d(TAG, "handleSongCompletion: Chế độ lặp không xác định hoặc không lặp.");
        }
    }

    private void playSongAtIndexLogic(boolean advanceToNext) {
        if (songList == null || songList.isEmpty()) return;

        int newIndex;
        if (advanceToNext) {
            newIndex = (currentTrackIndex < songList.size() - 1) ? currentTrackIndex + 1 : 0;
        } else {
            newIndex = (currentTrackIndex > 0) ? currentTrackIndex - 1 : songList.size() - 1;
        }
        currentTrackIndex = newIndex;
        currentSong = songList.get(currentTrackIndex);

        Log.d(TAG, "playSongAtIndexLogic: Chuẩn bị phát '" + currentSong.getTenBaiHat() + "' tại index " + currentTrackIndex);
        MusicService.play(this, currentSong, songList, currentTrackIndex); // Truyền context
        updateAllUIAndListenersForNewSong();

        checkAndUpdateFavoriteIcon(currentSong);
    }

    private void playNextSong() {
        Log.d(TAG, "Next button clicked");
        playSongAtIndexLogic(true);
    }

    private void playPreviousSong() {
        Log.d(TAG, "Previous button clicked");
        playSongAtIndexLogic(false);
    }

    private void updateAllUIAndListenersForNewSong() {
        updateSongInfoUI();
        updatePlayPauseIcon();
        initializeSeekBarAndUpdater();
        setCompletionListenerForCurrentPlayer();
    }

    private void showLyrics() {
        if (currentSong == null) {
            Log.w(TAG, "showLyrics: currentSong is null");
            return;
        }
        LyricsBottomSheetFragment lyricsFragment = new LyricsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("LRC_STRING", currentSong.getLoiBaiHat());
        lyricsFragment.setArguments(args);
        // lyricsFragment.setMediaPlayer(MusicService.getMediaPlayer()); // Fragment nên tự lấy nếu cần
        lyricsFragment.show(getSupportFragmentManager(), lyricsFragment.getTag());
    }

    private void updateSongInfoUI() {
        if (currentSong != null) {
            songName.setText(currentSong.getTenBaiHat());
            artistName.setText(currentSong.getCaSi());
            Glide.with(this).load(currentSong.getHinhBaiHat()).into(imageView);
        } else {
            Log.w(TAG, "updateSongInfoUI: currentSong is null");
            songName.setText("N/A");
            artistName.setText("N/A");
            if (imageView != null) { // Kiểm tra null cho imageView
                imageView.setImageResource(R.drawable.default_song_image);
            }
        }
    }

    private void updatePlayPauseIcon() {
        if (MusicService.isPlaying()) {
            playButton.setImageResource(R.drawable.ic_pause);
        } else {
            playButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void initializeSeekBarAndUpdater() {
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }

        MediaPlayer currentPlayer = MusicService.getMediaPlayer();
        if (currentPlayer != null) {
            try {
                int duration = currentPlayer.getDuration();
                seekBar.setMax(duration > 0 ? duration : 0);
                totalTimeText.setText(formatTime(duration));
                seekBar.setProgress(currentPlayer.getCurrentPosition());
                currentTimeText.setText(formatTime(currentPlayer.getCurrentPosition()));
            } catch (IllegalStateException e) {
                Log.e(TAG, "initializeSeekBarAndUpdater: Lỗi IllegalStateException", e);
                resetSeekBarUI();
            }
        } else {
            resetSeekBarUI();
        }

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer playerForRunnable = MusicService.getMediaPlayer();
                if (playerForRunnable != null) {
                    try {
                        if (playerForRunnable.isPlaying()) {
                            int currentPosition = playerForRunnable.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            currentTimeText.setText(formatTime(currentPosition));
                            // Không cần cập nhật max duration ở đây nữa vì initializeSeekBarAndUpdater đã làm khi chuyển bài
                        }
                    } catch (IllegalStateException e) {
                        // Player có thể đã được giải phóng
                    } finally {
                        handler.postDelayed(this, 500);
                    }
                } else {
                    resetSeekBarUI();
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(updateSeekBarRunnable);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // ... (giữ nguyên như trước) ...
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MediaPlayer playerToSeek = MusicService.getMediaPlayer();
                    if (playerToSeek != null) {
                        try {
                            playerToSeek.seekTo(progress);
                            currentTimeText.setText(formatTime(progress));
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "onProgressChanged: Lỗi IllegalStateException khi seek", e);
                        }
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (handler != null && updateSeekBarRunnable != null) {
                    handler.removeCallbacks(updateSeekBarRunnable);
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (handler != null && updateSeekBarRunnable != null) {
                    handler.post(updateSeekBarRunnable);
                }
            }
        });
    }

    private void resetSeekBarUI() {
        seekBar.setMax(0);
        totalTimeText.setText(formatTime(0));
        seekBar.setProgress(0);
        currentTimeText.setText(formatTime(0));
    }

    private String formatTime(int milliseconds) {
        if (milliseconds < 0) milliseconds = 0;
        int totalSeconds = milliseconds / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (songList != null && !songList.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < songList.size()) {
            currentSong = songList.get(currentTrackIndex);
        } else if (songList != null && !songList.isEmpty()) {
            currentTrackIndex = 0;
            currentSong = songList.get(currentTrackIndex);
        } else {
            Log.w(TAG, "onResume: songList là null hoặc rỗng, không thể cập nhật currentSong.");
            // Cần xử lý UI nếu không có bài hát
            updateSongInfoUI(); // Sẽ hiển thị N/A
            updatePlayPauseIcon();
            resetSeekBarUI();
            return; // Không làm gì thêm nếu không có danh sách bài hát
        }

        updateSongInfoUI();
        updatePlayPauseIcon();
        initializeSeekBarAndUpdater();
        setCompletionListenerForCurrentPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    // ham kiem tra khi trang thi tym khi chuyen sang bai hat moi
    private void checkAndUpdateFavoriteIcon(Song song) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) return;

        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<JsonObject> checkCall = apiService.isSongFavorited(userId, song.get_id());

        checkCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body().get("isFavorite").getAsBoolean();
                    ImageButton btnFavorite = findViewById(R.id.btnFavorite);
                    btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorited : R.drawable.ic_favorite);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("FavoriteCheck", "Lỗi khi kiểm tra yêu thích: " + t.getMessage());
            }
        });
    }

}

