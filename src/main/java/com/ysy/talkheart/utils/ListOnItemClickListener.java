package com.ysy.talkheart.utils;

import android.view.View;

/**
 * 自定义接口，然后在onBindViewHolder中去为holder.itemView去设置相应的监听最后回调设置的监听
 */
public interface ListOnItemClickListener {

    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);
}
