package com.example.music_app.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.music_app.Model.LyricLine;
import com.example.music_app.R;

import java.util.List;

public class LyricAdapter extends RecyclerView.Adapter<LyricAdapter.ViewHolder> {

    private List<LyricLine> lyrics;
    private int currentIndex = -1;

    public LyricAdapter(List<LyricLine> lyrics) {
        this.lyrics = lyrics;
    }

    public void setCurrentIndex(int index) {
        if (index == currentIndex) return;
        int old = currentIndex;
        currentIndex = index;
        if (old != -1) notifyItemChanged(old);
        notifyItemChanged(currentIndex);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lyricText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lyricText = itemView.findViewById(R.id.lyricTextView);
        }
    }

    @NonNull
    @Override
    public LyricAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyric_line_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LyricAdapter.ViewHolder holder, int position) {
        LyricLine line = lyrics.get(position);
        holder.lyricText.setText(line.getText());
        if (position == currentIndex) {
            holder.lyricText.setTextColor(Color.RED);
            holder.lyricText.setTextSize(18);
        } else {
            holder.lyricText.setTextColor(Color.BLACK);
            holder.lyricText.setTextSize(16);
        }
    }

    @Override
    public int getItemCount() {
        return lyrics.size();
    }
}
