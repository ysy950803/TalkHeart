package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.HomeFragment;
import com.ysy.talkheart.fragments.MeFragment;
import com.ysy.talkheart.fragments.MessageFragment;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.DBProcessor;
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

    private Handler homeHandler;
    private String UPDATE_URL = "";
    private static final int MSG_REFRESH_TIME = 24 * 1024;
    private static final int UPDATE_CHECK_TIME = 2048;
    private BottomNavigationBar bottomNavigationBar;

    private FloatingActionButton addFab;
    private ImageView msgUnreadImg;

    private String UID;
    private boolean isSeen = false;
    private int isRead = 1;

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public boolean getIsSeen() {
        return isSeen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        ActivitiesDestroyer.getInstance().killAll();
        initData();
        initView();
        homeHandler = new Handler();
        homeHandler.post(autoCheckUpdateRunnable);
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        isSeen = false;
    }

    private void initView() {
        msgUnreadImg = (ImageView) findViewById(R.id.msg_unread_img);
        msgUnreadImg.setVisibility(View.GONE);

        addFab = (FloatingActionButton) findViewById(R.id.home_add_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this, addFab, getString(R.string.trans_add));
                    startActivity(new Intent(HomeActivity.this, WriteActivity.class).putExtra("uid", UID), tAO.toBundle());
                } else
                    startActivity(new Intent(HomeActivity.this, WriteActivity.class).putExtra("uid", UID));
            }
        });

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.home_bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "首页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_message_white_24dp, "消息"))
                .addItem(new BottomNavigationItem(R.drawable.ic_person_pin_white_24dp, "个人"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setActiveColor("#2196F3")
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);
        setDefaultFragment();
    }

    @Override
    protected void onResume() {
        homeHandler.post(msgRefreshRunnable);
        super.onResume();
    }

    @Override
    protected void onStop() {
        homeHandler.removeCallbacks(msgRefreshRunnable);
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public BottomNavigationBar getBottomNavigationBar() {
        return bottomNavigationBar;
    }

    public ImageView getMsgUnreadImg() {
        return msgUnreadImg;
    }

    public FloatingActionButton getAddFab() {
        return addFab;
    }

    private void setMenuItemVisible(boolean feedback, boolean update, boolean search) {
        feedbackMenuItem.setVisible(feedback);
        updateMenuItem.setVisible(update);
        searchMenuItem.setVisible(search);
    }

    private void setDefaultFragment() {
        if (actionBar != null)
            actionBar.setTitle("首页");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideAllFragments(transaction);
        homeFragment = (HomeFragment) fm.findFragmentByTag("Home");
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance(UID);
            transaction.add(R.id.content_table_layout, homeFragment, "Home");
        } else
            transaction.show(homeFragment);
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = this.getSupportFragmentManager();
        // 开启事务
        FragmentTransaction transaction = fm.beginTransaction();
        hideAllFragments(transaction);
        switch (position) {
            case 0:
                homeFragment = (HomeFragment) fm.findFragmentByTag("Home");
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance(UID);
                    transaction.add(R.id.content_table_layout, homeFragment, "Home");
                } else
                    transaction.show(homeFragment);

                if (actionBar != null)
                    actionBar.setTitle("首页");
                setMenuItemVisible(false, false, true);
                addFab.setVisibility(View.VISIBLE);
                break;
            case 1:
                messageFragment = (MessageFragment) fm.findFragmentByTag("Msg");
                if (messageFragment == null) {
                    messageFragment = MessageFragment.newInstance(UID);
                    transaction.add(R.id.content_table_layout, messageFragment, "Msg");
                } else {
                    transaction.show(messageFragment);
                    if (isRead == 0)
                        messageFragment.getNewMsg();
                }

                if (actionBar != null)
                    actionBar.setTitle("消息");
                setMenuItemVisible(false, false, false);
                addFab.setVisibility(View.GONE);
                break;
            case 2:
                meFragment = (MeFragment) fm.findFragmentByTag("Me");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance(UID);
                    transaction.add(R.id.content_table_layout, meFragment, "Me");
                } else
                    transaction.show(meFragment);

                if (actionBar != null)
                    actionBar.setTitle("个人");
                setMenuItemVisible(true, true, false);
                addFab.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        // 事务提交
        transaction.commit();
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (homeFragment != null)
            transaction.hide(homeFragment);
        if (messageFragment != null)
            transaction.hide(messageFragment);
        if (meFragment != null)
            transaction.hide(meFragment);
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
                startActivity(new Intent(HomeActivity.this, FeedbackActivity.class).putExtra("uid", UID));
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

    private void handCheckUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker dbP = new UpdateChecker();
                if (dbP.getConn() != null) {
                    int code;
                    if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                        UPDATE_URL = dbP.downloadUrlSelect("select url from download_url where code = " + code);
                        homeHandler.post(updateRunnable);
                    } else
                        homeHandler.post(noUpdateRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    public void forceCheckUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker dbP = new UpdateChecker();
                if (dbP.getConn() != null) {
                    UPDATE_URL = dbP.downloadUrlSelect("select url from download_url where code = " + getVersionCode(getApplicationContext()));
                    homeHandler.post(updateRunnable);
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
            Toast.makeText(HomeActivity.this, "已经是最新版啦" + getVersionName(HomeActivity.this) + "，谢谢关注哦", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable msgRefreshRunnable = new Runnable() {
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn() != null) {
                        isRead = dbP.isReadSelect("select isread from user where uid = " + UID);
                        homeHandler.post(updateMsgIconRunnable);
                    }
                    dbP.closeConn();
                }
            }).start();
            // loop
            homeHandler.postDelayed(this, MSG_REFRESH_TIME);
        }
    };

    private Runnable updateMsgIconRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRead == 1)
                msgUnreadImg.setVisibility(View.GONE);
            else {
                msgUnreadImg.setVisibility(View.VISIBLE);
                if (lastSelectedPosition != 1)
                    messageFragment.getNewMsg();
            }
        }
    };

    private Runnable autoCheckUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UpdateChecker dbP = new UpdateChecker();
                    if (dbP.getConn() != null) {
                        int code;
                        if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                            UPDATE_URL = dbP.downloadUrlSelect("select url from download_url where code = " + code);
                            homeHandler.removeCallbacks(autoCheckUpdateRunnable);
                            homeHandler.post(updateRunnable);
                        } else
                            homeHandler.removeCallbacks(autoCheckUpdateRunnable);
                    }
                    dbP.closeConn();
                }
            }).start();
            homeHandler.postDelayed(this, UPDATE_CHECK_TIME);
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

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return "(v" + pi.versionName + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
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

    private long backTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - backTime) > 2000) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                backTime = System.currentTimeMillis();
            } else {
                Intent backHome = new Intent(Intent.ACTION_MAIN);
                backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                backHome.addCategory(Intent.CATEGORY_HOME);
                startActivity(backHome);
                return true;
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
