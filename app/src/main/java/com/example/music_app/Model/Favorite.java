package com.example.music_app.Model;

public class Favorite {
    private String _id;
    private String userId;
    private String songId;

    // Constructor
    public Favorite(String _id, String userId, String songId) {
        this._id = _id;
        this.userId = userId;
        this.songId = songId;
    }

    // Getters
    public String get_id() {
        return _id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSongId() {
        return songId;
    }
}
