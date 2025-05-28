package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.Model.Song;
import com.example.music_app.R;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private MyClickListenner myClickListenner;

    public SongAdapter(List<Song> songList, MyClickListenner myClickListenner) {
        this.songList = songList;
        this.myClickListenner = myClickListenner;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        // Set tên và ca sĩ
        holder.tvSongName.setText(song.getTenBaiHat());
        holder.tvArtist.setText(song.getCaSi());

        // Load ảnh bài hát bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(song.getHinhBaiHat())
                .into(holder.imgSong);

        // Xử lý click vào item
        holder.itemView.setOnClickListener(v -> {
            if (myClickListenner != null) {
                myClickListenner.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView tvSongName, tvArtist;
        ImageButton imgMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            tvSongName = itemView.findViewById(R.id.txtTenBaiHat);
            tvArtist = itemView.findViewById(R.id.txtCaSi);
            imgMore = itemView.findViewById(R.id.btnMore);
        }
    }

    // Giao diện bắt sự kiện click
    public interface MyClickListenner {
        void onItemClick(int position);
    }
}



