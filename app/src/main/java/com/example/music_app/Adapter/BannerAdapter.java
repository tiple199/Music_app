package com.example.music_app.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_app.Model.Quangcao;
import com.example.music_app.R;
import com.example.music_app.Server.GlideOkHttpClient;
import com.example.music_app.databinding.DongBannerBinding;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Quangcao> arrayListBanner;
    private OnBannerClickListener listener;

    public BannerAdapter(Context context, ArrayList<Quangcao> arrayListBanner, OnBannerClickListener listener) {
        this.context = context;
        this.arrayListBanner = arrayListBanner;
        this.listener = listener;

        // Khởi tạo OkHttpClient
        OkHttpClient client = GlideOkHttpClient.getOkHttpClient();

        // Cấu hình Glide để sử dụng OkHttpClient tùy chỉnh
        Glide.get(context).getRegistry().replace(GlideUrl.class, java.io.InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory) client));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DongBannerBinding binding = DongBannerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (arrayListBanner != null && position < arrayListBanner.size()) {
            Quangcao quangcao = arrayListBanner.get(position);
            String imageUrl = quangcao.getHinhanh();
            Log.d("BannerAdapter", "Loading image URL: " + imageUrl);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .apply(new RequestOptions()
//                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop())
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e("BannerAdapter", "Image load failed for URL: " + imageUrl + ", Error: " + (e != null ? e.getMessage() : "Unknown error"));
                                if (e != null) {
                                    e.logRootCauses("GlideException");
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d("BannerAdapter", "Image loaded successfully for URL: " + imageUrl);
                                return false;
                            }
                        })
                        .into(holder.binding.imageviewbackgroundbanner);
            } else {
                Log.w("BannerAdapter", "Invalid or empty image URL at position: " + position);
//                holder.binding.imageviewbackgroundbanner.setImageResource(R.drawable.placeholder_image);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBannerClick(quangcao);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayListBanner != null ? arrayListBanner.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        DongBannerBinding binding;

        public ViewHolder(@NonNull View itemView, DongBannerBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    public interface OnBannerClickListener {
        void onBannerClick(Quangcao quangcao);
    }
}