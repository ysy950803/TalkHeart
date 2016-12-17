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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
    private MenuItem feedbackMenuItem;
    private MenuItem updateMenuItem;
    private MenuItem searchMenuItem;

    private Handler updateHandler;
    private String UPDATE_URL = "";
    private static final int WAIT_TIME = 2048;

    private String UID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        ActivitiesDestroyer.getInstance().killAll();
//        ActivitiesDestroyer.getInstance().addActivity(this);

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
                autoCheckUpdate();
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

    private void setMenuItemVisible(boolean feedback, boolean update, boolean search) {
        feedbackMenuItem.setVisible(feedback);
        updateMenuItem.setVisible(update);
        searchMenuItem.setVisible(search);
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
                    homeFragment = HomeFragment.newInstance("Home", UID);
                }
                transaction.replace(R.id.content_table_layout, homeFragment);
                setMenuItemVisible(false, false, true);
                break;
            case 1:
                if (actionBar != null)
                    actionBar.setTitle("消息");
                if (messageFragment == null) {
                    messageFragment = MessageFragment.newInstance("Msg", "");
                }
                transaction.replace(R.id.content_table_layout, messageFragment);
                setMenuItemVisible(false, false, false);
                break;
            case 2:
                if (actionBar != null)
                    actionBar.setTitle("个人");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance("Me", UID);
                }
                transaction.replace(R.id.content_table_layout, meFragment);
                setMenuItemVisible(true, true, false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        feedbackMenuItem = menu.findItem(R.id.action_feedback);
        feedbackMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // startActivity
                return true;
            }
        });
        feedbackMenuItem.setVisible(false);

        updateMenuItem = menu.findItem(R.id.action_update);
        updateMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handCheckUpdate();
                return true;
            }
        });
        updateMenuItem.setVisible(false);

        searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class).putExtra("uid", UID));
                return true;
            }
        });
        searchMenuItem.setVisible(true);

        return true;
    }

    private void autoCheckUpdate() {
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

    private void handCheckUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker dbP = new UpdateChecker();
                if (dbP.getConn() != null) {
                    int code;
                    if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                        UPDATE_URL = dbP.downloadUrlSelect("select url from download_url where code = " + code);
                        updateHandler.post(updateRunnable);
                    } else
                        updateHandler.post(noUpdateRunnable);
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

    private Runnable noUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(HomeActivity.this, "已经是最新版啦，谢谢关注哦", Toast.LENGTH_SHORT).show();
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
                .setPositiveButton("非常乐意", new DialogInterface.OnClickListener() {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
