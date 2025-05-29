package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.Model.Artist;
import com.example.music_app.R;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private Context context;
    private List<Artist> artistList;
    private OnItemClickListener listener;

    // Giao diện xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Artist artist);
    }

    // Hàm set listener từ ngoài truyền vào
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ArtistAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.artist_item, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.txtTenNgheSi.setText(artist.getTenNgheSi());

        Glide.with(context)
                .load(artist.getHinhAnhNgheSi())
                .centerCrop()
                .into(holder.imgArtist);

        // Gọi callback khi click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(artist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgArtist;
        TextView txtTenNgheSi;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            imgArtist = itemView.findViewById(R.id.imgArtist);
            txtTenNgheSi = itemView.findViewById(R.id.txtTenNgheSi);
        }
    }
}




