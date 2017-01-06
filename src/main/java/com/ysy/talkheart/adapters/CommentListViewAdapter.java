package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.CommentActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/12/22.
 */

public class CommentListViewAdapter extends RecyclerView.Adapter<CommentListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private CommentActivity context;
    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public CommentListViewAdapter(CommentActivity context, List<Integer> avatarList, List<String> nicknameList, List<String> timeList, List<String> textList) {
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
    public CommentListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(final CommentListViewAdapter.RecyclerViewHolder holder, int position) {
        holder.avatarImg.setImageResource(avatarList.get(position));
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
        return avatarList.size();
    }
}
