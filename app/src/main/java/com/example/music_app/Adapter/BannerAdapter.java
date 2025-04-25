package com.example.music_app.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.music_app.Model.Quangcao;
import com.example.music_app.R;
import com.example.music_app.databinding.DongBannerBinding;

import java.util.ArrayList;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Quangcao> arrayListBanner;

    private OnBannerClickListener listener;

    public BannerAdapter(Context context, ArrayList<Quangcao> arrayListBanner, OnBannerClickListener listener) {
        this.context = context;
        this.arrayListBanner = arrayListBanner;
        this.listener = listener;
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
            Log.d("BannerAdapter", "Loading image URL: " + quangcao.getHinhanh());
            Glide.with(context)
                    .load(quangcao.getHinhanh())
//                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.binding.imageviewbackgroundbanner);
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
