package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Context context;

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Lưu lại context để dùng trong Glide
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.txtTenBaiHat.setText(song.getTenBaiHat());
        holder.txtCaSi.setText(song.getCaSi());

        Glide.with(context)
                .load(song.getHinhBaiHat())
                .placeholder(R.drawable.placeholder) // ảnh tạm nếu chưa tải xong
                .into(holder.imgSong);
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTenBaiHat, txtCaSi;
        ImageView imgSong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTenBaiHat = itemView.findViewById(R.id.txtTenBaiHat);
            txtCaSi = itemView.findViewById(R.id.txtCaSi);
            imgSong = itemView.findViewById(R.id.imgSong);
        }
    }
}

