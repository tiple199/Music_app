package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.music_app.Fragment.FragmentPlaylistDetail;
import com.example.music_app.Model.Playlist;
import com.example.music_app.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList;
    private Context context;

    public PlaylistAdapter(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.txtName.setText(playlist.getTen());
        Glide.with(context)
                .load(playlist.getHinhNen())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgBackground);

        // Xử lý khi click vào item
        holder.itemView.setOnClickListener(v -> {
            FragmentPlaylistDetail fragment = FragmentPlaylistDetail.newInstance(
                    playlist.get_id(), // playlistId
                    playlist.getTen()  // playlist name (để hiển thị tiêu đề)
            );

            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(android.R.id.content, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return playlistList != null ? playlistList.size() : 0;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgBackground;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPlaylistName);
            imgBackground = itemView.findViewById(R.id.imgPlaylist);
        }
    }
}
