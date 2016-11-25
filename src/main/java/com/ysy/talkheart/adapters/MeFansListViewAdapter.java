package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MeFansListViewAdapter extends RecyclerView.Adapter<MeFansListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> infoList;
    private List<Boolean> eachOtherList;

    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeFansListViewAdapter(List<Integer> avatarList, List<String> nicknameList, List<String> infoList, List<Boolean> eachOtherList) {
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.infoList = infoList;
        this.eachOtherList = eachOtherList;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView infoTv;
        ImageView eachOtherImg;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_fans_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_fans_nickname_tv);
            infoTv = (TextView) itemView.findViewById(R.id.me_fans_info_tv);
            eachOtherImg = (ImageView) itemView.findViewById(R.id.me_fans_each_other_img);
        }
    }

    @Override
    public MeFansListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_fans, parent, false));
    }

    @Override
    public void onBindViewHolder(final MeFansListViewAdapter.RecyclerViewHolder holder, int position) {
        holder.avatarImg.setImageResource(avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.infoTv.setText(infoList.get(position));

        final int pos = Integer.parseInt(position + "");
        final ImageView eachOther = holder.eachOtherImg;
        eachOther.setImageResource(eachOtherList.get(position) ? R.mipmap.ic_swap_horiz_pink_36dp : R.mipmap.ic_swap_horiz_blue_36dp);
        eachOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!eachOtherList.get(pos)) {
                    eachOther.setImageResource(R.mipmap.ic_swap_horiz_pink_36dp);
                    eachOtherList.set(pos, true);
                } else {
                    eachOther.setImageResource(R.mipmap.ic_swap_horiz_blue_36dp);
                    eachOtherList.set(pos, false);
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