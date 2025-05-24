package com.example.music_app.Model;

import java.io.Serializable;

public class LyricLine implements Serializable {
    private long timestamp;
    private String text;

    public LyricLine(long timestamp, String text) {
        this.timestamp = timestamp;
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }
}
