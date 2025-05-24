package com.example.music_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.music_app.Fragment.LyricsBottomSheetFragment;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.MusicService;

import java.io.IOException;
import java.util.ArrayList;

public class SongActivity extends AppCompatActivity {

    private enum RepeatMode {
        REPEAT_ALL,
        REPEAT_ONE,
    }

    private RepeatMode repeatMode = RepeatMode.REPEAT_ALL;
    private ArrayList<Song> songList;
    private Song currentSong;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private ImageButton repeatButton, playButton, nextButton, previousButton, randomButton, btnClose;
    private ImageView imageView;
    private TextView currentTimeText, totalTimeText, songName, artistName;
    private Handler handler;
    private Runnable updateSeekBarRunnable;
    private int currentTrackIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Intent intent = getIntent();
        songList = (ArrayList<Song>) intent.getSerializableExtra("SONG_LIST");
        currentTrackIndex = intent.getIntExtra("SELECTED_INDEX", 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "Danh sách bài hát rỗng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mediaPlayer = new MediaPlayer();
        handler = new Handler();

        seekBar = findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton_test);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        randomButton = findViewById(R.id.randomButton);
        repeatButton = findViewById(R.id.repeatButton);
        songName = findViewById(R.id.name_song);
        artistName = findViewById(R.id.artist_name);
        imageView = findViewById(R.id.image_song);
        btnClose = findViewById(R.id.btnClose);
        currentSong = songList.get(currentTrackIndex);

        btnClose.setOnClickListener(v -> {

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            finish(); // Quay lại Fragment
            overridePendingTransition(0, R.anim.slide_out_down);
        });

        playTrackAt(currentTrackIndex);

        Button btnShowLyrics = findViewById(R.id.btnShowLyrics);
        btnShowLyrics.setOnClickListener(v -> {
            LyricsBottomSheetFragment fragment = new LyricsBottomSheetFragment();
            Bundle args = new Bundle();
            args.putString("LRC_STRING", songList.get(currentTrackIndex).getLoiBaiHat());
            fragment.setArguments(args);
            fragment.setMediaPlayer(mediaPlayer);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        nextButton.setOnClickListener(v -> {
            currentTrackIndex = (currentTrackIndex + 1) % songList.size();
            playTrackAt(currentTrackIndex);
        });

        previousButton.setOnClickListener(v -> {
            currentTrackIndex = (currentTrackIndex - 1 + songList.size()) % songList.size();
            playTrackAt(currentTrackIndex);
        });

        randomButton.setOnClickListener(v -> {
            int newIndex;
            do {
                newIndex = (int) (Math.random() * songList.size());
            } while (newIndex == currentTrackIndex && songList.size() > 1);
            currentTrackIndex = newIndex;
            playTrackAt(currentTrackIndex);
        });

        playButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playButton.setImageResource(R.drawable.ic_play);
            } else if (mediaPlayer != null) {
                mediaPlayer.start();
                playButton.setImageResource(R.drawable.ic_pause);
                updateSeekBar();
            }
        });

        updateSeekBarRunnable = () -> {
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    updateTimeDisplay();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            handler.postDelayed(updateSeekBarRunnable, 1000);
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        repeatButton.setOnClickListener(v -> {
            switch (repeatMode) {
                case REPEAT_ALL:
                    repeatMode = RepeatMode.REPEAT_ONE;
                    repeatButton.setImageResource(R.drawable.repeat_one_icon);
                    break;
                case REPEAT_ONE:
                    repeatMode = RepeatMode.REPEAT_ALL;
                    repeatButton.setImageResource(R.drawable.repeat_all_icon);
                    break;
            }
        });
    }

    private void playTrackAt(int index) {
        if (mediaPlayer != null && index >= 0 && index < songList.size()) {
            mediaPlayer.reset();
            Song song = songList.get(index);
            try {
                mediaPlayer.setDataSource(song.getLinkBaiHat());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.ic_pause);
                    seekBar.setMax(mediaPlayer.getDuration());
                    updateSeekBar();
                    updateTimeDisplay();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        playTrackAt(currentTrackIndex);
                    } else {
                        currentTrackIndex = (currentTrackIndex + 1) % songList.size();
                        playTrackAt(currentTrackIndex);
                    }
                });
                songName.setText(song.getTenBaiHat());
                artistName.setText(song.getCaSi());
                Glide.with(this).load(song.getHinhBaiHat())
                        .placeholder(R.drawable.playholder)
                        .error(R.drawable.error)
                        .into(imageView);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể phát bài hát", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateTimeDisplay() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int totalDuration = mediaPlayer.getDuration();
        currentTimeText.setText(formatTime(currentPosition));
        totalTimeText.setText(formatTime(totalDuration));
    }

    private String formatTime(int timeInMillis) {
        int minutes = timeInMillis / 1000 / 60;
        int seconds = timeInMillis / 1000 % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}