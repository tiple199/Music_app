package com.example.music_app.Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Artist implements Serializable {
    @SerializedName("tenNgheSi")
    private String name;
    @SerializedName("hinhAnh")
    private String imageUrl;

    public Artist(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

