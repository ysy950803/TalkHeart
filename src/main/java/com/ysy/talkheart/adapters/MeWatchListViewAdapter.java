package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MeWatchListViewAdapter extends RecyclerView.Adapter<MeWatchListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> infoList;
    private List<Integer> relationList; // 0:watch 1:each_other -1:fans -2:nothing

    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeWatchListViewAdapter(List<Integer> avatarList, List<String> nicknameList, List<String> infoList, List<Integer> relationList) {
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.infoList = infoList;
        this.relationList = relationList;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView infoTv;
        ImageView eachOtherImg;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_watch_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_watch_nickname_tv);
            infoTv = (TextView) itemView.findViewById(R.id.me_watch_info_tv);
            eachOtherImg = (ImageView) itemView.findViewById(R.id.me_watch_each_other_img);
        }
    }

    @Override
    public MeWatchListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_watch, parent, false));
    }

    @Override
    public void onBindViewHolder(final MeWatchListViewAdapter.RecyclerViewHolder holder, int position) {
        holder.avatarImg.setImageResource(avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.infoTv.setText(infoList.get(position));

        final int pos = Integer.parseInt(position + "");
        final ImageView eachOther = holder.eachOtherImg;

        eachOther.setImageResource(relationList.get(position) == 1 ? R.mipmap.ic_each_other_pink_36dp : R.mipmap.ic_watch_blue_pink_36dp);
        eachOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relationList.get(pos) == 0) {
                    eachOther.setImageResource(R.mipmap.ic_nothing_blue_36dp);
                    relationList.set(pos, -2);
                } else if (relationList.get(pos) == 1) {
                    eachOther.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
                    relationList.set(pos, -1);
                } else if (relationList.get(pos) == -1) {
                    eachOther.setImageResource(R.mipmap.ic_each_other_pink_36dp);
                    relationList.set(pos, 1);
                } else {
                    eachOther.setImageResource(R.mipmap.ic_watch_blue_pink_36dp);
                    relationList.set(pos, 0);
                }
            }
        });

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
        return nicknameList.size();
    }

}
