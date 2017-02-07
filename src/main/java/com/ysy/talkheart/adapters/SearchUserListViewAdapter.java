package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.SearchActivity;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/12/11.
 */

public class SearchUserListViewAdapter extends SuperRecyclerViewAdapter {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> infoList;
    private SearchActivity context;

    private String AVATAR_UPLOAD_URL = "";

    public SearchUserListViewAdapter(SearchActivity context, List<String> uidList,
                                     List<Integer> avatarList, List<String> nicknameList, List<String> infoList) {
        this.context = context;
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.infoList = infoList;
        this.AVATAR_UPLOAD_URL = context.getString(R.string.url_avatar_upload);
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView introTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.search_user_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.search_user_nickname_tv);
            introTv = (TextView) itemView.findViewById(R.id.search_user_intro_tv);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false));
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        downloadAvatar(context, AVATAR_UPLOAD_URL + "/" + uidList.get(position) + "_avatar_img_thumb.jpg",
                holder.avatarImg, avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.introTv.setText(infoList.get(position));

        super.onBindViewHolder(viewHolder, position);
    }
}

