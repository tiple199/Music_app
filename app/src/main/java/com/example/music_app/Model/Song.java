package com.example.music_app.Model;

import java.util.List;

public class Song {
    private String _id;
    private String TenBaiHat;
    private String CaSi;
    private String HinhBaiHat;
    private String LinkBaiHat;
    private int LuotThich;
    private List<String> albumIds;
    private List<String> theLoaiIds;
    private List<String> playListIds;

    public Song() {
    }

    public Song(String _id, String tenBaiHat, String caSi, String hinhBaiHat, String linkBaiHat, int luotThich,
                List<String> albumIds, List<String> theLoaiIds, List<String> playListIds) {
        this._id = _id;
        TenBaiHat = tenBaiHat;
        CaSi = caSi;
        HinhBaiHat = hinhBaiHat;
        LinkBaiHat = linkBaiHat;
        LuotThich = luotThich;
        this.albumIds = albumIds;
        this.theLoaiIds = theLoaiIds;
        this.playListIds = playListIds;
    }

    // Getter & Setter

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTenBaiHat() {
        return TenBaiHat;
    }

    public void setTenBaiHat(String tenBaiHat) {
        TenBaiHat = tenBaiHat;
    }

    public String getCaSi() {
        return CaSi;
    }

    public void setCaSi(String caSi) {
        CaSi = caSi;
    }

    public String getHinhBaiHat() {
        return HinhBaiHat;
    }

    public void setHinhBaiHat(String hinhBaiHat) {
        HinhBaiHat = hinhBaiHat;
    }

    public String getLinkBaiHat() {
        return LinkBaiHat;
    }

    public void setLinkBaiHat(String linkBaiHat) {
        LinkBaiHat = linkBaiHat;
    }

    public int getLuotThich() {
        return LuotThich;
    }

    public void setLuotThich(int luotThich) {
        LuotThich = luotThich;
    }

    public List<String> getAlbumIds() {
        return albumIds;
    }

    public void setAlbumIds(List<String> albumIds) {
        this.albumIds = albumIds;
    }

    public List<String> getTheLoaiIds() {
        return theLoaiIds;
    }

    public void setTheLoaiIds(List<String> theLoaiIds) {
        this.theLoaiIds = theLoaiIds;
    }

    public List<String> getPlayListIds() {
        return playListIds;
    }

    public void setPlayListIds(List<String> playListIds) {
        this.playListIds = playListIds;
    }
}
