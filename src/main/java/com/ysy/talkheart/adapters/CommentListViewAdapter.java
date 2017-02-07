package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.CommentActivity;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/12/22.
 */

public class CommentListViewAdapter extends SuperRecyclerViewAdapter {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private CommentActivity context;
    private String AVATAR_UPLOAD_URL = "";

    public CommentListViewAdapter(CommentActivity context, List<String> uidList,
                                  List<Integer> avatarList, List<String> nicknameList,
                                  List<String> timeList, List<String> textList) {
        this.context = context;
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        ImageView replyImg;
        TextView contentTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.comment_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.comment_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.comment_time_tv);
            replyImg = (ImageView) itemView.findViewById(R.id.comment_reply_img);
            contentTv = (TextView) itemView.findViewById(R.id.comment_text_tv);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        downloadAvatar(holder.avatarImg, uidList.get(position), avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.contentTv.setText(textList.get(position));
        holder.timeTv.setText(timeList.get(position));

        final int pos = Integer.parseInt(position + "");

        holder.replyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else
                    context.reply(pos);
            }
        });

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

        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    private void downloadAvatar(final CircularImageView avatarImg, String uid, final int defaultResId) {
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
        Glide.with(context).load(AVATAR_UPLOAD_URL + "/" + uid + "_avatar_img_thumb.jpg")
                .asBitmap()
                .signature(new StringSignature("" + System.currentTimeMillis()))
                .placeholder(defaultResId)
                .error(defaultResId)
                .into(avatarImg);
    }
}
