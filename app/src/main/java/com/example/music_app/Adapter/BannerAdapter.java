package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_app.Model.Quangcao;
import com.example.music_app.R;
import com.example.music_app.databinding.DongBannerBinding;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private final Context context;
    private final List<Quangcao> banners;
    private final OnBannerClickListener listener;

    public BannerAdapter(Context context, List<Quangcao> banners, OnBannerClickListener listener) {
        this.context = context;
        this.banners = banners;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DongBannerBinding binding = DongBannerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quangcao quangcao = banners.get(position);
        holder.bind(quangcao);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final DongBannerBinding binding;

        ViewHolder(DongBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Quangcao quangcao) {
            String imageUrl = quangcao.getHinhanh();
            if (imageUrl != null && !imageUrl.isEmpty()) { // Sửa lỗi ở đây
                Glide.with(context)
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .error(R.drawable.error_image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop())
                        .into(binding.imageviewbackgroundbanner);
            } else {
                binding.imageviewbackgroundbanner.setImageResource(R.drawable.error_image);
            }

            binding.getRoot().setOnClickListener(v -> listener.onBannerClick(quangcao));
        }
    }

    public interface OnBannerClickListener {
        void onBannerClick(Quangcao quangcao);
    }
}