package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * 列表适配器
 * Created by Shengyu Yao on 2016/7/7.
 */
public class HomeListViewAdapter extends RecyclerView.Adapter<HomeListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;

    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public HomeListViewAdapter(List<Integer> avatarList, List<String> nicknameList, List<String> timeList, List<String> textList) {
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.home_active_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.home_active_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.home_active_time_tv);
            textTv = (TextView) itemView.findViewById(R.id.home_active_text_tv);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_home_active, parent, false));
    }

    @Override
    public int getItemCount() {
        return nicknameList.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder viewHolder, int position) {
        viewHolder.avatarImg.setImageResource(avatarList.get(position));
        viewHolder.nicknameTv.setText(nicknameList.get(position));
        viewHolder.timeTv.setText(timeList.get(position));
        viewHolder.textTv.setText(textList.get(position));

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(viewHolder.itemView, pos);
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(viewHolder.itemView, pos);
                    return false;
                }
            });
        }
    }

}