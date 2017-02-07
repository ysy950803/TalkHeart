package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MeWatchListViewAdapter extends SuperRecyclerViewAdapter {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> introList;
    private List<Integer> relationList; // 0:watch 1:each_other -1:fans -2:nothing
    private WatchActivity context;
    private boolean isObserver;

    private String AVATAR_UPLOAD_URL = "";

    public MeWatchListViewAdapter(WatchActivity context, List<String> uidList,
                                  List<Integer> avatarList, List<String> nicknameList,
                                  List<String> introList, List<Integer> relationList, boolean isObserver) {
        this.context = context;
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.introList = introList;
        this.relationList = relationList;
        this.isObserver = isObserver;
        this.AVATAR_UPLOAD_URL = context.getString(R.string.url_avatar_upload);
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView introTv;
        ImageView eachOtherImg;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_watch_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_watch_nickname_tv);
            introTv = (TextView) itemView.findViewById(R.id.me_watch_intro_tv);
            eachOtherImg = (ImageView) itemView.findViewById(R.id.me_watch_each_other_img);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_watch, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        downloadAvatar(context, AVATAR_UPLOAD_URL + "/" + uidList.get(position) + "_avatar_img_thumb.jpg",
                holder.avatarImg, avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.introTv.setText(introList.get(position));

        final int pos = Integer.parseInt(position + "");
        final ImageView eachOther = holder.eachOtherImg;
        if (isObserver)
            eachOther.setVisibility(View.GONE);
        else {
            eachOther.setImageResource(relationList.get(position) == 2 ? R.mipmap.ic_each_other_pink_36dp : R.mipmap.ic_watch_blue_pink_36dp);
            eachOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context);
                    if (!cd.isConnectingToInternet()) {
                        Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    } else {
                        context.eachOtherImg = eachOther;
                        context.updateRelation(pos);
                    }
                }
            });
        }

        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return nicknameList.size();
    }
}
