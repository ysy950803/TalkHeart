package com.ysy.talkheart.utils;

import android.content.DialogInterface;
import java.util.Calendar;

/**
 * Created by Shengyu Yao on 2017/1/6.
 */

public abstract class NoDoubleDialogClickListener implements DialogInterface.OnClickListener {

    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(dialog, which);
        }
    }

    protected abstract void onNoDoubleClick(DialogInterface dialog, int which);
}
