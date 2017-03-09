package com.ysy.talkheart.utils;

import android.support.v7.widget.RecyclerView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by Shengyu Yao on 2016/12/18.
 */

public abstract class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private int mScrollThreshold = 4;
    private boolean isScrolled = false;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
        if (isSignificantDelta && !isScrolled) {
            if (dy > 0)
                onScrollUp();
            else
                onScrollDown();
            isScrolled = true;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        isScrolled = false;
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }
}
