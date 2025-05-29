package com.example.music_app.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.Model.Playlist;
import com.example.music_app.Model.Song;
import com.example.music_app.R;
import com.example.music_app.Service.APIRetrofitClient;
import com.example.music_app.Service.APIService;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songList;
    private final MyClickListenner myClickListenner;
    private final String contextType; // "favorite", "playlist", "search", ...
    private String playlistId; // lưu playlist đang xem

    public SongAdapter(List<Song> songList, MyClickListenner listener, String contextType, String playlistId) {
        this.songList = songList;
        this.myClickListenner = listener;
        this.contextType = contextType;
        this.playlistId = playlistId;
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
        holder.tvSongName.setText(song.getTenBaiHat());
        holder.tvArtist.setText(song.getCaSi());

        String imageUrl = song.getHinhBaiHat();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            holder.imgSong.setImageResource(R.drawable.default_song_image);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img_default_song)
                    .error(R.drawable.img_default_song)
                    .into(holder.imgSong);
        }

        holder.itemView.setOnClickListener(v -> {
            if (myClickListenner != null) {
                myClickListenner.onItemClick(position);
            }
        });

        holder.btnMoreSong.setOnClickListener(v -> showPopupMenu(v, song, position));
    }

    private void showPopupMenu(View view, Song song, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        Context context = view.getContext();

        switch (contextType) {
            case "favorite":
                popupMenu.getMenu().add("Thêm vào playlist");
                popupMenu.getMenu().add("Xóa khỏi yêu thích");
                break;
            case "playlist":
                popupMenu.getMenu().add("Xóa khỏi playlist");
                break;
            default:
                popupMenu.getMenu().add("Thêm vào playlist");
                popupMenu.getMenu().add("Phát ngay");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            if (selected.equals("Xóa khỏi yêu thích")) {
                removeFromFavorite(context, song.get_id(), position);
            } else if (selected.equals("Thêm vào playlist")) {
                showAddToPlaylistDialog(view.getContext(), song.get_id());
            } else if (selected.equals("Phát bài sau đó")) {
                Toast.makeText(context, "Đã thêm vào hàng chờ", Toast.LENGTH_SHORT).show();
                // Thêm vào hàng chờ phát nhạc
            } else if (selected.equals("Phát ngay")) {
                Toast.makeText(context, "Đang phát...", Toast.LENGTH_SHORT).show();
                // Xử lý phát bài
            } else if (selected.equals("Xóa khỏi playlist")) {
                if (playlistId == null) {
                    Toast.makeText(context, "Không có playlistId để xóa", Toast.LENGTH_SHORT).show();
                    return true;
                }
                removeSongFromPlaylist(context, playlistId, song.get_id(), position);
            }

            return true;
        });

        popupMenu.show();
    }

    // Xoa bài hat khoi danh sach ua thich
    private void removeFromFavorite(Context context, String songId, int position) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(context, "Không xác định người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);

        Call<Void> call = apiService.removeFavorite(userId, songId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    songList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView tvSongName, tvArtist;
        ImageButton btnMoreSong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            tvSongName = itemView.findViewById(R.id.txtTenBaiHat);
            tvArtist = itemView.findViewById(R.id.txtCaSi);
            btnMoreSong = itemView.findViewById(R.id.btnMoreSong);
        }
    }

    public interface MyClickListenner {
        void onItemClick(int position);
    }

    // Cum de xu ly khi them bai hat vao playlist
    private void showAddToPlaylistDialog(Context context, String songId) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<List<Playlist>> call = apiService.getUserPlaylists(userId);

        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Playlist> playlists = response.body();
                    showPlaylistDialog(context, playlists, songId);
                } else {
                    Toast.makeText(context, "Không thể tải danh sách phát", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistDialog(Context context, List<Playlist> playlists, String songId) {
        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = playlists.get(i).getTen();
        }

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Thêm vào Playlist")
                .setItems(names, (dialog, which) -> {
                    String playlistId = playlists.get(which).get_id();
                    addSongToPlaylist(context, playlistId, songId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addSongToPlaylist(Context context, String playlistId, String songId) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<ResponseBody> call = apiService.addSongToPlaylist(playlistId, songId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Đã thêm vào playlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //

    // Xu ly xoa bai hat khoi playlist
    private void removeSongFromPlaylist(Context context, String playlistId, String songId, int position) {
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<ResponseBody> call = apiService.removeSongFromPlaylist(playlistId, songId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    songList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa khỏi playlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
