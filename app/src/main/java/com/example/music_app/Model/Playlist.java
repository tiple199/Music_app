package com.example.music_app.Model;

public class Playlist {
    private String _id;
    private String Ten;
    private String HinhNen;

    public Playlist() {}

    public Playlist(String _id, String ten, String hinhNen) {
        this._id = _id;
        Ten = ten;
        HinhNen = hinhNen;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTen() {
        return Ten;
    }

    public void setTen(String ten) {
        Ten = ten;
    }

    public String getHinhNen() {
        return HinhNen;
    }

    public void setHinhNen(String hinhNen) {
        HinhNen = hinhNen;
    }
}

