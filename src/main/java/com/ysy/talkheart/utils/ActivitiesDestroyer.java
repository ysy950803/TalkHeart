package com.ysy.talkheart.utils;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/21.
 */

public class ActivitiesDestroyer extends Application {
    // 运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<>();
    // 为了实现每次使用该类时不创建新的对象而创建的静态对象
    private static ActivitiesDestroyer instance;

    // 构造方法
    private ActivitiesDestroyer() {
    }

    // 实例化一次
    public synchronized static ActivitiesDestroyer getInstance() {
        if (null == instance) {
            instance = new ActivitiesDestroyer();
        }
        return instance;
    }

    // 添加Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    // 关闭每一个list内的activity
    public void killAll() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 杀进程
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
}