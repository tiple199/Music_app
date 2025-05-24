
package com.example.music_app.Fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.LyricAdapter;
import com.example.music_app.Model.LyricLine;
import com.example.music_app.R;
import com.example.music_app.Service.MusicService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricsBottomSheetFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private LyricAdapter adapter;
    private List<LyricLine> lyrics = new ArrayList<>();
    private Handler handler = new Handler();


    private String lrcString;
    private Runnable updateRunnable;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics_bottom_sheet, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewLyrics);

        if (getArguments() != null) {
            lrcString = getArguments().getString("LRC_STRING", "");
            lrcString = lrcString.replace("\\n", "\n");
        }

        lyrics = parseLrc(lrcString);
        adapter = new LyricAdapter(lyrics);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        startSyncLyrics();

        return view;
    }

    private void startSyncLyrics() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Luôn lấy MediaPlayer hiện tại từ MusicService
                MediaPlayer currentPlayer = MusicService.getMediaPlayer(); // Giả sử MusicService có phương thức này và nó là static

                if (currentPlayer != null && currentPlayer.isPlaying()) {
                    long currentTime = currentPlayer.getCurrentPosition();
                    int currentIndex = getCurrentLyricIndex(currentTime);
                    adapter.setCurrentIndex(currentIndex);

                }
                handler.postDelayed(this, 100);
            }
        };
        handler.post(updateRunnable);
    }

    private int getCurrentLyricIndex(long currentTimeMs) {
        long correctedTime = currentTimeMs;
        for (int i = 0; i < lyrics.size() - 1; i++) {
            if (correctedTime >= lyrics.get(i).getTimestamp()
                    && correctedTime < lyrics.get(i + 1).getTimestamp()) {
                return i;
            }
        }
        return lyrics.size() - 1;
    }

    private List<LyricLine> parseLrc(String lrcText) {
        List<LyricLine> lines = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+).(\\d+)](.*)");
        String[] split = lrcText.split("\n");
        for (String line : split) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int min = Integer.parseInt(matcher.group(1));
                int sec = Integer.parseInt(matcher.group(2));
                int millis = Integer.parseInt(matcher.group(3));
                long timestamp = (min * 60 + sec) * 1000 + millis;
                String text = matcher.group(4).trim();
                lines.add(new LyricLine(timestamp, text));
            }
        }
        return lines;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }
}
