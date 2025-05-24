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
    private MediaPlayer mediaPlayer;

    // Hardcoded LRC string (có thể sau này truyền từ server)
    private String lrcString;
    private Runnable updateRunnable;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics_bottom_sheet, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewLyrics); // nhớ thêm ID này trong XML
        if (getArguments() != null) {
            lrcString = getArguments().getString("LRC_STRING", "");
            lrcString = lrcString.replace("\\n", "\n");

        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        lyrics = parseLrc(lrcString);
        adapter = new LyricAdapter(lyrics);
        recyclerView.setAdapter(adapter);

        startSyncLyrics();

        return view;
    }

    private void startSyncLyrics() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    long currentTime = mediaPlayer.getCurrentPosition();
                    int currentIndex = getCurrentLyricIndex(currentTime);
                    adapter.setCurrentIndex(currentIndex);
                    recyclerView.scrollToPosition(currentIndex);
                }
                handler.postDelayed(this, 300); // cập nhật mượt hơn
            }
        };
        handler.post(updateRunnable);
    }


    private int getCurrentLyricIndex(long currentTimeMs) {
        long correctedTime = currentTimeMs + 300; // offset bù trễ
        for (int i = 0; i < lyrics.size() - 1; i++) {
            if (correctedTime >= lyrics.get(i).getTimestamp()
                    && correctedTime < lyrics.get(i + 1).getTimestamp()) {
                return i;
            }
        }
        return lyrics.size() - 1;
    }



    private List<LyricLine> parseLrc(String lrc) {
        List<LyricLine> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+).(\\d+)](.*)");
        Matcher matcher = pattern.matcher(lrc);
        while (matcher.find()) {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));
            int millis = Integer.parseInt(matcher.group(3));
            String text = matcher.group(4).trim();
            long timestamp = (minutes * 60 + seconds) * 1000L + millis;
            result.add(new LyricLine(timestamp, text));
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}
