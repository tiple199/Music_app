package com.example.music_app.Model;

import com.google.gson.annotations.SerializedName;

public class Quangcao {
    @SerializedName("IdQuangCao") // Khớp với JSON
    private String idQuangCao;

    @SerializedName("Hinhanh")    // Khớp với JSON
    private String hinhanh;

    @SerializedName("Idbaihat")   // Khớp với JSON
    private String idbaihat;

    public String getIdQuangCao() {
        return idQuangCao;
    }

    public void setIdQuangCao(String idQuangCao) {
        this.idQuangCao = idQuangCao;
    }

    public String getHinhanh() {
        return hinhanh;
    }

    public void setHinhanh(String hinhanh) {
        this.hinhanh = hinhanh;
    }

    public String getIdbaihat() {
        return idbaihat;
    }

    public void setIdbaihat(String idbaihat) {
        this.idbaihat = idbaihat;
    }
}