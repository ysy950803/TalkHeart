package com.ysy.talkheart.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.HomeFragment;
import com.ysy.talkheart.fragments.MeFragment;
import com.ysy.talkheart.fragments.MessageFragment;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.UpdateChecker;

public class HomeActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {

    private HomeFragment homeFragment;
    private MessageFragment messageFragment;
    private MeFragment meFragment;
    int lastSelectedPosition = 0;
    private ActionBar actionBar;

    private Handler updateHandler;
    private String UPDATE_URL = "";
    private static final int WAIT_TIME = 2048;

    private String UID = "加载中…";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        ActivitiesDestroyer.getInstance().killAll();

        UID = getIntent().getExtras().getString("uid");

        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "首页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_message_white_24dp, "消息"))
                .addItem(new BottomNavigationItem(R.drawable.ic_person_pin_white_24dp, "个人"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setActiveColor("#2196F3")
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);
        setDefaultFragment();

        updateHandler = new Handler();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                checkUpdate();
            }
        }, WAIT_TIME);
    }

    private void setDefaultFragment() {
        if (actionBar != null)
            actionBar.setTitle("首页");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        homeFragment = HomeFragment.newInstance("First", UID);
        transaction.replace(R.id.content_table_layout, homeFragment);
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = this.getSupportFragmentManager();
        // 开启事务
        FragmentTransaction transaction = fm.beginTransaction();
        switch (position) {
            case 0:
                if (actionBar != null)
                    actionBar.setTitle("首页");
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance("First", UID);
                }
                transaction.replace(R.id.content_table_layout, homeFragment);
                break;
            case 1:
                if (actionBar != null)
                    actionBar.setTitle("消息");
                if (messageFragment == null) {
                    messageFragment = MessageFragment.newInstance("Second", "Message");
                }
                transaction.replace(R.id.content_table_layout, messageFragment);
                break;
            case 2:
                if (actionBar != null)
                    actionBar.setTitle("个人");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance("Third", UID);
                }
                transaction.replace(R.id.content_table_layout, meFragment);
                break;
            default:
                break;
        }
        // 事务提交
        transaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker dbP = new UpdateChecker();
                if (dbP.getConn() != null) {
                    int code;
                    if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                        UPDATE_URL = dbP.downloadUrlSelect("select url from download_url where code = " + code);
                        updateHandler.post(updateRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateDialog(UPDATE_URL);
        }
    };

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateDialog(final String url) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("兴高采烈的提示框").setMessage("检测到有新版本哦（新功能、修复已知错误等），快快下载吧！").setCancelable(false)
                .setPositiveButton("义不容辞", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                }).setNegativeButton("再想想", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
