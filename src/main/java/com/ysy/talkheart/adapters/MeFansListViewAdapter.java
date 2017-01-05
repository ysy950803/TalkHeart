package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.FansActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MeFansListViewAdapter extends RecyclerView.Adapter<MeFansListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> infoList;
    private List<Integer> relationList;
    private FansActivity context;
    private ListOnItemClickListener mOnItemClickListener;
    private boolean isObserver;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeFansListViewAdapter(FansActivity context, List<Integer> avatarList, List<String> nicknameList,
                                 List<String> infoList, List<Integer> relationList, boolean isObserver) {
        this.context = context;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.infoList = infoList;
        this.relationList = relationList;
        this.isObserver = isObserver;
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
        if (isObserver)
            eachOther.setVisibility(View.GONE);
        else {
            eachOther.setImageResource(relationList.get(position) == 2 ? R.mipmap.ic_each_other_pink_36dp : R.mipmap.ic_fans_pink_blue_36dp);
            eachOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context);
                    if (!cd.isConnectingToInternet()) {
                        Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    } else {
                        context.eachOtherImg = eachOther;
                        context.eachOtherImg.setClickable(false);
                        switch (relationList.get(pos)) {
                            case 2:
                                eachOther.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
                                relationList.set(pos, -1);
                                context.updateRelation(pos, -1);
                                break;
                            case -1:
                                eachOther.setImageResource(R.mipmap.ic_each_other_pink_36dp);
                                relationList.set(pos, 2);
                                context.updateRelation(pos, 2);
                                break;
                        }
                    }
                }
            });
        }

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