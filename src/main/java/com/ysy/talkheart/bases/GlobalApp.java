package com.ysy.talkheart.bases;

import android.app.Application;

/**
 * Created by Shengyu Yao on 2017/1/12.
 */

public class GlobalApp extends Application {

    private boolean isMeInfoUpdated = false;

    public void setMeInfoUpdated(boolean meInfoUpdated) {
        isMeInfoUpdated = meInfoUpdated;
    }

    public boolean getMeInfoUpdated() {
        return isMeInfoUpdated;
    }
}
