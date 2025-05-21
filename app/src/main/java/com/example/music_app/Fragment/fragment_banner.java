package com.example.music_app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.music_app.Adapter.BannerAdapter;
import com.example.music_app.Model.Quangcao;
import com.example.music_app.databinding.FragmentBannerBinding;
import java.util.ArrayList;
import java.util.List;
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_banner extends Fragment {
    private FragmentBannerBinding binding;
    private ViewPager2 viewPager;
    private CircleIndicator3 circleIndicator;
    private BannerAdapter bannerAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoSlideRunnable;
    private int currentItem;

    private static final long SLIDE_INTERVAL_MS = 4500L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBannerBinding.inflate(inflater, container, false);
        initViews();
//        fetchBanners();
        return binding.getRoot();
    }

    private void initViews() {
        viewPager = binding.viewpager;
        circleIndicator = binding.indicatordefault;
    }

//    private void fetchBanners() {
//        binding.progressBar.setVisibility(View.VISIBLE);
//        APIService.getService().GetDataBanner().enqueue(new Callback<List<Quangcao>>() {
//            @Override
//            public void onResponse(Call<List<Quangcao>> call, Response<List<Quangcao>> response) {
//                binding.progressBar.setVisibility(View.GONE);
//                if (response.isSuccessful()) {
//                    List<Quangcao> banners = response.body();
//                    if (banners == null || banners.isEmpty()) {
//                        showToast("No banners available");
//                        return;
//                    }
//                    setupBannerAdapter(banners);
//                    setupAutoSlide();
//                } else {
//                    showToast("Failed to load banners");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Quangcao>> call, Throwable t) {
//                binding.progressBar.setVisibility(View.GONE);
//                showToast("Failed to load banners");
//            }
//        });
//    }

    private void setupBannerAdapter(List<Quangcao> banners) {
        bannerAdapter = new BannerAdapter(requireContext(), new ArrayList<>(banners), quangcao -> {
            // Handle banner click if needed
        });
        viewPager.setAdapter(bannerAdapter);
        circleIndicator.setViewPager(viewPager);
    }

    private void setupAutoSlide() {
        if (viewPager.getAdapter() == null || viewPager.getAdapter().getItemCount() == 0) {
            return;
        }

        autoSlideRunnable = () -> {
            currentItem = viewPager.getCurrentItem() + 1;
            if (currentItem >= viewPager.getAdapter().getItemCount()) {
                currentItem = 0;
            }
            viewPager.setCurrentItem(currentItem, true);
            handler.postDelayed(autoSlideRunnable, SLIDE_INTERVAL_MS);
        };
        handler.postDelayed(autoSlideRunnable, SLIDE_INTERVAL_MS);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoSlideRunnable != null) {
            handler.removeCallbacks(autoSlideRunnable);
        }
        binding = null;
    }
}