package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/23.
 */

public class MeDraftListViewAdapter extends SuperRecyclerViewAdapter {

    private List<String> timeList;
    private List<String> textList;

    public MeDraftListViewAdapter(List<String> timeList, List<String> textList) {
        this.timeList = timeList;
        this.textList = textList;
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView timeTv;
        TextView textTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            timeTv = (TextView) itemView.findViewById(R.id.me_draft_time);
            textTv = (TextView) itemView.findViewById(R.id.me_draft_text);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_draft, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        holder.timeTv.setText(timeList.get(position));
        holder.textTv.setText(textList.get(position));

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }
}
