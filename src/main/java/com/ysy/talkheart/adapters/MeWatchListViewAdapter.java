package com.ysy.talkheart.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MeWatchListViewAdapter extends RecyclerView.Adapter<MeWatchListViewAdapter.RecyclerViewHolder> {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> introList;
    private List<Integer> relationList; // 0:watch 1:each_other -1:fans -2:nothing
    private ListOnItemClickListener mOnItemClickListener;
    private WatchActivity context;
    private boolean isObserver;

    private String AVATAR_UPLOAD_URL = "";

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

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
        this.AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
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
        downloadAvatar(holder.avatarImg, uidList.get(position), avatarList.get(position));
        holder.avatarImg.setImageResource(avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.infoTv.setText(introList.get(position));

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

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new NoDoubleViewClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
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
