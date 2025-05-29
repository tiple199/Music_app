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
import com.example.music_app.Model.Album;
import com.example.music_app.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<Album> albumList;
    private OnItemClickListener listener;

    // ✅ Giao diện xử lý khi click vào album
    public interface OnItemClickListener {
        void onItemClick(Album album);
    }

    // ✅ Phương thức để set listener từ ngoài
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ✅ Constructor adapter
    public AlbumAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        holder.txtTenAlbum.setText(album.getTenAlbum());
        holder.txtTenCaSi.setText(album.getTenCaSiAlbum());

        Glide.with(context)
                .load(album.getHinhAlbum())
                .into(holder.imgHinhAlbum);

        // ✅ Gọi sự kiện click khi người dùng bấm vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList != null ? albumList.size() : 0;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHinhAlbum;
        TextView txtTenAlbum, txtTenCaSi;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHinhAlbum = itemView.findViewById(R.id.imgHinhAlbum);
            txtTenAlbum = itemView.findViewById(R.id.txtTenAlbum);
            txtTenCaSi = itemView.findViewById(R.id.txtTenCaSi);
        }
    }
}




