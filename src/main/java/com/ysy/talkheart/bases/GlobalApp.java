package com.ysy.talkheart.bases;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2017/1/12.
 */

public class GlobalApp extends Application {

    private boolean isMeInfoUpdated = false;
    private boolean isHomeActiveUpdated = false;

    public void setMeInfoUpdated(boolean meInfoUpdated) {
        isMeInfoUpdated = meInfoUpdated;
    }

    public boolean getMeInfoUpdated() {
        return isMeInfoUpdated;
    }

    public void setHomeActiveUpdated(boolean homeActiveUpdated) {
        isHomeActiveUpdated = homeActiveUpdated;
    }

    public boolean getHomeActiveUpdated() {
        return  isHomeActiveUpdated;
    }
}
