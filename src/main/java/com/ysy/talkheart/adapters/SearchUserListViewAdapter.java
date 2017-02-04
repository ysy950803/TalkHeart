package com.ysy.talkheart.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.SearchActivity;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Shengyu Yao on 2016/12/11.
 */

public class SearchUserListViewAdapter extends RecyclerView.Adapter<SearchUserListViewAdapter.RecyclerViewHolder> {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> infoList;
    private ListOnItemClickListener mOnItemClickListener;
    private SearchActivity context;

    private String AVATAR_UPLOAD_URL = "";

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public SearchUserListViewAdapter(SearchActivity context, List<String> uidList,
                                     List<Integer> avatarList, List<String> nicknameList, List<String> infoList) {
        this.context = context;
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.infoList = infoList;
        this.AVATAR_UPLOAD_URL = context.getString(R.string.url_avatar_upload);
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView infoTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.search_user_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.search_user_nickname_tv);
            infoTv = (TextView) itemView.findViewById(R.id.search_user_info_tv);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false));
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder viewHolder, int position) {
        downloadAvatar(viewHolder.avatarImg, uidList.get(position), avatarList.get(position));
        viewHolder.nicknameTv.setText(nicknameList.get(position));
        viewHolder.infoTv.setText(infoList.get(position));

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new NoDoubleViewClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
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

    private void downloadAvatar(final CircularImageView avatarImg, String uid, final int defaultResId) {
        new AsyncHttpClient().get(AVATAR_UPLOAD_URL + "/" + uid + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Bitmap picBmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                        if (picBmp != null) {
                            avatarImg.setImageBitmap(picBmp);
                        } else
                            avatarImg.setImageResource(defaultResId);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        avatarImg.setImageResource(defaultResId);
                    }
                });
    }
}

