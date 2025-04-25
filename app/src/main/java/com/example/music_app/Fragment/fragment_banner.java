package com.example.music_app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.music_app.Server.APIService;
import com.example.music_app.Server.Dataservice;
import com.example.music_app.databinding.FragmentBannerBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragment_banner extends Fragment {
    private FragmentBannerBinding binding;
    private ViewPager2 viewPager2;
    private CircleIndicator3 circleIndicator;
    private BannerAdapter bannerAdapter;
    private Handler handler;
    private Runnable runnable;
    private int currentItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBannerBinding.inflate(inflater, container, false);
        init();
        getData();
        return binding.getRoot();
    }

    private void init() {
        viewPager2 = binding.viewpager;
        circleIndicator = binding.indicatordefault;
    }

    private void getData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Dataservice dataservice = APIService.getService();
        Call<List<Quangcao>> callback = dataservice.GetDataBanner();
        callback.enqueue(new Callback<List<Quangcao>>() {
            @Override
            public void onResponse(Call<List<Quangcao>> call, Response<List<Quangcao>> response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d("BannerFragment", "Response Code: " + response.code());
                if (response.isSuccessful()) {
                    // Kiểm tra Content-Type
                    String contentType = response.headers().get("Content-Type");
                    Log.d("BannerFragment", "Content-Type: " + contentType);
                    if (contentType == null || !contentType.contains("application/json")) {
                        Log.e("BannerFragment", "Unexpected Content-Type: " + contentType);
                        try {
                            String rawBody = response.body() != null ? new Gson().toJson(response.body()) : "Empty body";
                            Log.e("BannerFragment", "Raw Response: " + rawBody);
                        } catch (Exception e) {
                            Log.e("BannerFragment", "Failed to read raw body: " + e.getMessage());
                        }
                        Toast.makeText(requireContext(), "Server returned invalid data format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        List<Quangcao> banners = response.body();
                        if (banners == null) {
                            Log.w("BannerFragment", "Banner list is null");
                            Toast.makeText(requireContext(), "No banners available", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ArrayList<Quangcao> bannerList = new ArrayList<>(banners);
                        Log.d("BannerFragment", "Banners size: " + bannerList.size());
                        for (Quangcao q : bannerList) {
                            Log.d("BannerFragment", "Banner URL: " + q.getHinhanh());
                        }
                        if (bannerList.isEmpty()) {
                            Log.w("BannerFragment", "Banner list is empty");
                            Toast.makeText(requireContext(), "No banners available", Toast.LENGTH_SHORT).show();
                        } else {
                            bannerAdapter = new BannerAdapter(requireContext(), bannerList, quangcao -> {
                                Log.d("BannerFragment", "Clicked on banner: " + quangcao.getIdQuangCao());
                            });
                            viewPager2.setAdapter(bannerAdapter);
                            circleIndicator.setViewPager(viewPager2);
                            setupAutoSlide();
                        }
                    } catch (Exception e) {
                        Log.e("BannerFragment", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("BannerFragment", "Response failed. Code: " + response.code() + ", Message: " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("BannerFragment", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("BannerFragment", "Failed to read error body: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), "Failed to load banners", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Quangcao>> call, Throwable throwable) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("BannerFragment", "API call failed: " + throwable.getMessage());
                throwable.printStackTrace();
                Toast.makeText(requireContext(), "Failed to load banners", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoSlide() {
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            if (viewPager2.getAdapter() != null && viewPager2.getAdapter().getItemCount() > 0) {
                currentItem = viewPager2.getCurrentItem();
                currentItem++;
                if (currentItem >= viewPager2.getAdapter().getItemCount()) {
                    currentItem = 0;
                }
                viewPager2.setCurrentItem(currentItem, true);
                handler.postDelayed(runnable, 4500);
            }
        };
        if (viewPager2.getAdapter() != null && viewPager2.getAdapter().getItemCount() > 0) {
            handler.postDelayed(runnable, 4500);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        binding = null;
    }
}