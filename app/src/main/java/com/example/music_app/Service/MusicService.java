package com.example.music_app.Service;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.example.music_app.Model.Song;

import java.util.ArrayList; // Thêm import này

public class MusicService {
    private static MediaPlayer mediaPlayer;
    private static Song currentSong;
    private static ArrayList<Song> currentPlaylist; // Biến mới để lưu danh sách phát
    private static int currentIndexInPlaylist = -1;  // Biến mới để lưu vị trí hiện tại
    private static final String TAG = "MusicService";

    // Sửa đổi phương thức play để nhận thêm playlist và index
    public static void play(Context context, Song song, ArrayList<Song> playlist, int index) {
        if (song == null || song.getLinkBaiHat() == null || song.getLinkBaiHat().isEmpty()) {
            Log.e(TAG, "Không thể phát: Song hoặc LinkBaiHat là null hoặc rỗng.");
            releasePlayer();
            currentSong = null;
            currentPlaylist = null;
            currentIndexInPlaylist = -1;
            return;
        }

        // Lưu trữ thông tin playlist và index mới
        MusicService.currentPlaylist = playlist;
        MusicService.currentIndexInPlaylist = index;

        if (mediaPlayer != null && currentSong != null && currentSong.get_id().equals(song.get_id())) {
            // Yêu cầu phát cùng một bài hát (ví dụ: REPEAT_ONE hoặc người dùng chọn lại)
            try {
                Log.d(TAG, "Phát lại bài hát: " + song.getTenBaiHat());
                if (!mediaPlayer.isPlaying()) { // Chỉ phát lại nếu không đang phát
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                } else { // Nếu đang phát rồi và lại được gọi play (ít khi xảy ra với REPEAT_ONE đúng)
                    mediaPlayer.seekTo(0); // Vẫn seek về 0 để đảm bảo phát từ đầu nếu cần
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "Lỗi IllegalStateException khi phát lại " + song.getTenBaiHat() + ". Tạo lại player.", e);
                releasePlayer();
                createAndStartPlayer(context, song); // currentPlaylist và currentIndex đã được set ở trên
            }
        } else {
            // Bài hát mới hoặc chưa có player
            Log.d(TAG, "Phát bài hát mới: " + song.getTenBaiHat());
            releasePlayer();
            createAndStartPlayer(context, song); // currentPlaylist và currentIndex đã được set ở trên
        }
    }

    private static void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi trong khi mediaPlayer.release()", e);
            }
            mediaPlayer = null;
        }
    }

    private static void createAndStartPlayer(Context context, Song song) {
        if (song == null || song.getLinkBaiHat() == null || song.getLinkBaiHat().isEmpty()) {
            Log.e(TAG, "Không thể tạo player: Song hoặc LinkBaiHat là null/rỗng trong createAndStartPlayer.");
            currentSong = null; // Không gán currentSong nếu không tạo được
            // currentPlaylist và currentIndexInPlaylist giữ nguyên giá trị được truyền vào play()
            return;
        }
        try {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(song.getLinkBaiHat()));
            if (mediaPlayer != null) {
                currentSong = song; // Chỉ gán currentSong nếu player được tạo thành công
                mediaPlayer.start();
                Log.d(TAG, "Player đã được tạo và bắt đầu cho: " + song.getTenBaiHat());
            } else {
                Log.e(TAG, "MediaPlayer.create() trả về null cho: " + song.getLinkBaiHat());
                currentSong = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo/bắt đầu player cho: " + song.getLinkBaiHat(), e);
            releasePlayer();
            currentSong = null;
        }
    }

    public static void togglePlayPause() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "Lỗi IllegalStateException trong togglePlayPause", e);
            }
        }
    }

    public static boolean isPlaying() {
        if (mediaPlayer == null) return false;
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            Log.w(TAG, "Lỗi IllegalStateException khi kiểm tra isPlaying()", e);
            return false;
        }
    }

    public static Song getCurrentSong() {
        return currentSong;
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    // Getters mới cho playlist và index
    public static ArrayList<Song> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public static int getCurrentIndexInPlaylist() {
        return currentIndexInPlaylist;
    }
}