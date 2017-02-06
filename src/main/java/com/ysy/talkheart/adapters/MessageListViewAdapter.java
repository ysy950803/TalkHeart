package com.ysy.talkheart.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.MessageFragment;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MessageListViewAdapter extends RecyclerView.Adapter<MessageListViewAdapter.RecyclerViewHolder> {

    private String AVATAR_UPLOAD_URL = "";

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nameActList;
    private List<String> timeList;
    private List<String> contentList;
    private List<String> quoteList;
    private MessageFragment context;
    private ListOnItemClickListener mOnItemClickListener;

    private final int NORMAL_TYPE = R.layout.item_message;
    private final int FOOT_TYPE = R.layout.item_foot_loading;
    private int maxExistCount = 9;
    private boolean isLoading = true;

    private FootLoadCallBack loadCallBack;

    public interface FootLoadCallBack {
        void onLoad();
    }

    public MessageListViewAdapter(MessageFragment context, List<String> uidList,
                                  List<Integer> avatarList, List<String> nameActList,
                                  List<String> timeList, List<String> contentList, List<String> quoteList) {
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nameActList = nameActList;
        this.timeList = timeList;
        this.contentList = contentList;
        this.quoteList = quoteList;
        this.context = context;
        this.AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nameActTv;
        TextView timeTv;
        TextView contentTv;
        TextView quoteTv;
        ImageView replyImg;
        LinearLayout quoteLayout;

        ProgressBar loadingPBar;
        TextView loadingTv;

        RecyclerViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == R.layout.item_message) {
                avatarImg = (CircularImageView) itemView.findViewById(R.id.msg_avatar_img);
                nameActTv = (TextView) itemView.findViewById(R.id.msg_name_act_tv);
                timeTv = (TextView) itemView.findViewById(R.id.msg_time_tv);
                contentTv = (TextView) itemView.findViewById(R.id.msg_content_tv);
                quoteTv = (TextView) itemView.findViewById(R.id.msg_quote_tv);
                replyImg = (ImageView) itemView.findViewById(R.id.msg_reply_img);
                quoteLayout = (LinearLayout) itemView.findViewById(R.id.msg_quote_layout);
            } else {
                loadingPBar = (ProgressBar) itemView.findViewById(R.id.foot_loading_progressbar);
                loadingTv = (TextView) itemView.findViewById(R.id.foot_loading_tv);
            }
        }
    }

    @Override
    public MessageListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new RecyclerViewHolder(LayoutInflater
//                .from(parent.getContext())
//                .inflate(R.layout.item_message, parent, false));
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (viewType == FOOT_TYPE)
            return new RecyclerViewHolder(view, FOOT_TYPE);
        else
            return new RecyclerViewHolder(view, NORMAL_TYPE);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < maxExistCount)
            return NORMAL_TYPE;
        if (position == getItemCount() - 1)
            return FOOT_TYPE;
        else
            return NORMAL_TYPE;
    }

    @Override
    public int getItemCount() {
        return nameActList.size() < (maxExistCount + 1) ? nameActList.size() : nameActList.size() + 1;
    }

    @Override
    public void onBindViewHolder(final MessageListViewAdapter.RecyclerViewHolder holder, int position) {
        if (getItemViewType(position) == NORMAL_TYPE) {
            downloadAvatar(holder.avatarImg, uidList.get(position), avatarList.get(position));
            holder.nameActTv.setText(nameActList.get(position));
            holder.timeTv.setText(timeList.get(position));
            holder.quoteTv.setText(quoteList.get(position));

            if (contentList.get(position).equals(""))
                holder.contentTv.setVisibility(View.GONE);
            else {
                holder.contentTv.setVisibility(View.VISIBLE);
                holder.contentTv.setText(contentList.get(position));
            }

            final int pos = Integer.parseInt(position + "");
            holder.replyImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    else
                        context.reply(pos);
                }
            });

            holder.quoteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    else
                        context.openComment(pos);
                }
            });

            holder.avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
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
        } else {
            if (isLoading) {
                holder.loadingPBar.setVisibility(View.VISIBLE);
                holder.loadingTv.setText(R.string.content_loading);
                loadCallBack.onLoad();
            } else {
                holder.loadingPBar.setVisibility(View.GONE);
                holder.loadingTv.setText(R.string.content_loading_fail);
                holder.loadingTv.setOnClickListener(new NoDoubleViewClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {
                        isLoading = true;
                        holder.loadingPBar.setVisibility(View.VISIBLE);
                        holder.loadingTv.setText(R.string.content_loading);
                        loadCallBack.onLoad();
                    }
                });
            }
        }
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

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setFootLoadCallBack(FootLoadCallBack loadCallBack) {
        this.loadCallBack = loadCallBack;
    }

    public void setMaxExistCount(int maxExistCount) {
        this.maxExistCount = maxExistCount;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }
}
