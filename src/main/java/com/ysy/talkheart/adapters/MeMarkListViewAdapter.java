package com.ysy.talkheart.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.MarkActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Shengyu Yao on 2016/11/23.
 */

public class MeMarkListViewAdapter extends RecyclerView.Adapter<MeMarkListViewAdapter.RecyclerViewHolder> {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private MarkActivity context;
    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeMarkListViewAdapter(MarkActivity context, List<String> uidList,
                                 List<Integer> avatarList, List<String> nicknameList, List<String> timeList, List<String> textList) {
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.context = context;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_mark_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_mark_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.me_mark_time_tv);
            textTv = (TextView) itemView.findViewById(R.id.me_mark_text_tv);
        }
    }

    @Override
    public MeMarkListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_mark, parent, false));
    }

    @Override
    public void onBindViewHolder(final MeMarkListViewAdapter.RecyclerViewHolder holder, int position) {
        downloadAvatar(holder.avatarImg, uidList.get(position), avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.timeTv.setText(timeList.get(position));
        holder.textTv.setText(textList.get(position));

        final int pos = Integer.parseInt(position + "");

        holder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else
                    context.openPerson(pos);
            }
        });

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
        new AsyncHttpClient().get(context.getResources().getString(R.string.url_avatar_upload) + "/" + uid + "_avatar_img_thumb.jpg",
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
