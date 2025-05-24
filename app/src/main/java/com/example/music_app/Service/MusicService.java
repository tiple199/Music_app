package com.example.music_app.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MusicService extends Service {
    private static MediaPlayer mediaPlayer;
    private static String currentUrl;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("AUDIO_URL");

        if (mediaPlayer != null && url != null && !url.equals(currentUrl)) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mediaPlayer == null && url != null) {
            mediaPlayer = new MediaPlayer();
            currentUrl = url;
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}