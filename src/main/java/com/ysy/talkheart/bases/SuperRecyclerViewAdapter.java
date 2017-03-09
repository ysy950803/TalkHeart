package com.ysy.talkheart.bases;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

/**
 * Created by Shengyu Yao on 2017/2/7.
 */

public abstract class SuperRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
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
        return 0;
    }

    protected void downloadAvatar(Context context, String url, CircularImageView avatarImg, int defaultResId) {
//        AsyncHttpClient httpClient = new AsyncHttpClient();
//        httpClient.setTimeout(16 * 1000);
//        httpClient.get(AVATAR_UPLOAD_URL + "/" + uid + "_avatar_img_thumb.jpg",
//                new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        Bitmap picBmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
//                        if (picBmp != null) {
//                            avatarImg.setImageBitmap(picBmp);
//                        } else
//                            avatarImg.setImageResource(defaultResId);
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                        avatarImg.setImageResource(defaultResId);
//                    }
//                });
        Glide.with(context).load(url)
                .asBitmap()
                .signature(new StringSignature("" + System.currentTimeMillis()))
                .placeholder(defaultResId)
                .error(defaultResId)
                .into(avatarImg);
    }

    protected int maxExistCount = 9;
    protected boolean isLoading = true;

    private ListOnItemClickListener mOnItemClickListener;
    protected FootLoadCallBack loadCallBack;

    public interface FootLoadCallBack {
        void onLoad();
    }

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setMaxExistCount(int maxExistCount) {
        this.maxExistCount = maxExistCount;
    }

    public void setFootLoadCallBack(FootLoadCallBack loadCallBack) {
        this.loadCallBack = loadCallBack;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }
}
