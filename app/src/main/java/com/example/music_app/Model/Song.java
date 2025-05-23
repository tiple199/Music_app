package com.example.music_app.Model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Song implements Serializable {

    @SerializedName("_id") // hoặc "id" nếu JSON key là "id"
    private String id;

    @SerializedName("TenBaiHat")
    private String title;

    @SerializedName("CaSi")
    private String artist;

    @SerializedName("url")
    private String url;

    public Song(String id, String title, String artist, String url) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.url = url;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getUrl() { return url; }
}


