package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ListOnItemClickListener;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/23.
 */

public class MeDraftListViewAdapter extends RecyclerView.Adapter<MeDraftListViewAdapter.RecyclerViewHolder> {

    private List<String> timeList;
    private List<String> textList;

    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeDraftListViewAdapter(List<String> timeList, List<String> textList) {
        this.timeList = timeList;
        this.textList = textList;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView timeTv;
        TextView textTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            timeTv = (TextView) itemView.findViewById(R.id.me_draft_time);
            textTv = (TextView) itemView.findViewById(R.id.me_draft_text);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_draft, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        holder.timeTv.setText(timeList.get(position));
        holder.textTv.setText(textList.get(position));

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }
}
