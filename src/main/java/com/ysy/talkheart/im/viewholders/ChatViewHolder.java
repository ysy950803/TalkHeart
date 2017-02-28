package com.ysy.talkheart.im.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public abstract class ChatViewHolder<T> extends RecyclerView.ViewHolder {

    ChatViewHolder(Context context, ViewGroup root, int layoutRes) {
        super(LayoutInflater.from(context).inflate(layoutRes, root, false));
        ButterKnife.bind(this, itemView);
    }

    public Context getContext() {
        return itemView.getContext();
    }

    public abstract void bindData(T t);

    public void setData(T t) {
        bindData(t);
    }
}
