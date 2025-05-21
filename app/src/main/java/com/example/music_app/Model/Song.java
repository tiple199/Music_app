package com.example.music_app.Model;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String title;
    private String artist;
    private String url;

    public Song(String id, String title, String artist, String url) {
        this.id = id;
        this.title = title;
        this.artist = artist;

        this.url = url;
    }

    // Thêm các getter và setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }

    public String getUrl() { return url; }
}

