package com.ysy.talkheart.utils;

import android.view.MenuItem;

import java.util.Calendar;

/**
 * Created by Shengyu Yao on 2017/1/2.
 */

public abstract class NoDoubleMenuItemClickListener implements MenuItem.OnMenuItemClickListener {

    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(item);
        }
        return true;
    }

    protected abstract void onNoDoubleClick(MenuItem item);

}
