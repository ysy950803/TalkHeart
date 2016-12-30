package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.MessageFragment;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/25.
 */

public class MessageListViewAdapter extends RecyclerView.Adapter<MessageListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nameActList;
    private List<String> timeList;
    private List<String> contentList;
    private List<String> quoteList;
    private MessageFragment context;

    private ListOnItemClickListener mOnItemClickListener;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MessageListViewAdapter(MessageFragment context, List<Integer> avatarList, List<String> nameActList, List<String> timeList, List<String> contentList, List<String> quoteList) {
        this.avatarList = avatarList;
        this.nameActList = nameActList;
        this.timeList = timeList;
        this.contentList = contentList;
        this.quoteList = quoteList;
        this.context = context;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nameActTv;
        TextView timeTv;
        TextView contentTv;
        TextView quoteTv;
        ImageView replyImg;
        LinearLayout quoteLayout;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.msg_avatar_img);
            nameActTv = (TextView) itemView.findViewById(R.id.msg_name_act_tv);
            timeTv = (TextView) itemView.findViewById(R.id.msg_time_tv);
            contentTv = (TextView) itemView.findViewById(R.id.msg_content_tv);
            quoteTv = (TextView) itemView.findViewById(R.id.msg_quote_tv);
            replyImg = (ImageView) itemView.findViewById(R.id.msg_reply_img);
            quoteLayout = (LinearLayout) itemView.findViewById(R.id.msg_quote_layout);
        }
    }

    @Override
    public MessageListViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(final MessageListViewAdapter.RecyclerViewHolder holder, int position) {
        holder.avatarImg.setImageResource(avatarList.get(position));
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
        return nameActList.size();
    }
}
