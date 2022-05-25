package com.example.pc.lbs.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pc.lbs.R;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KeyTimeAdapter extends RecyclerView.Adapter<KeyTimeAdapter.ViewHolder> {

    List<String> keyTimeList;

    public KeyTimeAdapter(List<String> keyTimeList) {
        this.keyTimeList = keyTimeList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_key_time, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.keyTimeValueTV.setOnClickListener(v -> {
            //传递被点击的是第几个
            int position = viewHolder.getAdapterPosition();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            bundle.putString("keyTimeVal", keyTimeList.get(position));
            mListener.onClick(bundle);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        String keyTime = keyTimeList.get(position);
        String keyTimeInfo = "关键时间点" + (position + 1) + "：" + keyTime;
        holder.keyTimeValueTV.setText(keyTimeInfo);
    }

    @Override
    public int getItemCount() {
        return keyTimeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyTimeValueTV;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            keyTimeValueTV = itemView.findViewById(R.id.tv_key_time_value);
        }
    }

    public interface OnKeyTimeClickListener {
        void onClick(Bundle bundle);
    }

    public void setOnKeyTimeClickListener(OnKeyTimeClickListener listener) {
        mListener = listener;
    }

    private OnKeyTimeClickListener mListener;


}
