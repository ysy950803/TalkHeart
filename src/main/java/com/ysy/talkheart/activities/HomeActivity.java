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
import com.ysy.talkheart.utils.DataProcessor;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.NoDouleDialogClickListener;
import com.ysy.talkheart.utils.UpdateChecker;

public class HomeActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {

    private HomeFragment homeFragment;
    private MessageFragment messageFragment;
    private MeFragment meFragment;
    int curSelectedPosition = 0;
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
    private long backTime;
    private String[] opts_o;
    private String[] opts_t;
    private String UPDATE_DETAIL = "检测到有新版本哦，快快下载吧！";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        ActivitiesDestroyer.getInstance().killAll();
        ActivitiesDestroyer.getInstance().addActivity(this);
        initData();
        initView();
        homeHandler = new Handler();
        homeHandler.post(autoCheckUpdateRunnable);
    }

    @Override
    protected void onResume() {
        homeHandler.post(msgRefreshRunnable);
        if (meFragment != null && meFragment.isAdded() && !meFragment.isHidden()) {
            meFragment.getMeInfo();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        homeHandler.removeCallbacks(msgRefreshRunnable);
        super.onStop();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        opts_o = getIntent().getExtras().getStringArray("opts_o");
        opts_t = getIntent().getExtras().getStringArray("opts_t");
        isSeen = false;
    }

    private void initView() {
        msgUnreadImg = (ImageView) findViewById(R.id.msg_unread_img);
        msgUnreadImg.setVisibility(View.GONE);

        addFab = (FloatingActionButton) findViewById(R.id.home_add_fab);
        addFab.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(HomeActivity.this, WriteActivity.class);
                intent.putExtra("opts_o", opts_o);
                intent.putExtra("uid", UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this, addFab, getString(R.string.trans_add));
                    startActivity(intent, tAO.toBundle());
                } else
                    startActivity(intent);
            }
        });

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.home_bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "首页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_message_white_24dp, "消息"))
                .addItem(new BottomNavigationItem(R.drawable.ic_person_pin_white_24dp, "个人"))
                .setFirstSelectedPosition(curSelectedPosition)
                .setActiveColor("#2196F3")
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);
        setDefaultFragment();
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (homeFragment != null)
            transaction.hide(homeFragment);
        if (messageFragment != null)
            transaction.hide(messageFragment);
        if (meFragment != null)
            transaction.hide(meFragment);
    }

    private void setDefaultFragment() {
        if (actionBar != null)
            actionBar.setTitle("首页");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideAllFragments(transaction);
        homeFragment = (HomeFragment) fm.findFragmentByTag("Home");
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance(UID, opts_o);
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
                    homeFragment = HomeFragment.newInstance(UID, opts_o);
                    transaction.add(R.id.content_table_layout, homeFragment, "Home");
                } else
                    transaction.show(homeFragment);
                curSelectedPosition = 0;
                if (actionBar != null)
                    actionBar.setTitle("首页");
                setMenuItemVisible(false, false, true);
                addFab.setVisibility(View.VISIBLE);
                break;
            case 1:
                messageFragment = (MessageFragment) fm.findFragmentByTag("Msg");
                if (messageFragment == null) {
                    messageFragment = MessageFragment.newInstance(UID, opts_o);
                    transaction.add(R.id.content_table_layout, messageFragment, "Msg");
                } else {
                    transaction.show(messageFragment);
                    if (isRead == 0)
                        messageFragment.getNewMsg();
                }
                curSelectedPosition = 1;
                if (actionBar != null)
                    actionBar.setTitle("消息");
                setMenuItemVisible(false, false, false);
                addFab.setVisibility(View.GONE);
                break;
            case 2:
                meFragment = (MeFragment) fm.findFragmentByTag("Me");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance(UID, opts_o);
                    transaction.add(R.id.content_table_layout, meFragment, "Me");
                } else
                    transaction.show(meFragment);
                curSelectedPosition = 2;
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

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    private void handCheckUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker dbP = new UpdateChecker();
                if (dbP.getConn(opts_t) != null) {
                    int code;
                    if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                        String res[] = dbP.urlAndDetailSelect("select url, detail from download_url u, details d " +
                                "where d.code = u.code and d.code = " + code);
                        UPDATE_URL = res[0];
                        if (res[1] == null)
                            res[1] = "";
                        UPDATE_DETAIL = "检测到有新版本哦，快快下载吧！\n" + res[1];
                        homeHandler.post(updateRunnable);
                    } else {
                        String res = dbP.detailSelect("select detail from details d where code = " + getVersionCode(getApplicationContext()));
                        if (res == null)
                            res = "";
                        UPDATE_DETAIL = "谢谢关注哦！本版已更新内容：\n" + res;
                        homeHandler.post(noUpdateRunnable);
                    }
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
                if (dbP.getConn(opts_t) != null) {
                    String res[] = dbP.urlAndDetailSelect("select url, detail from download_url u, details d " +
                            "where d.code = u.code and d.code = " + getVersionCode(getApplicationContext()));
                    UPDATE_URL = res[0];
                    if (res[1] == null)
                        res[1] = "";
                    UPDATE_DETAIL = "检测到有新版本哦，快快下载吧！\n" + res[1];
                    homeHandler.post(updateRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void updateDialog(final String url) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("兴高采烈的提示框").setMessage(UPDATE_DETAIL).setCancelable(false)
                .setPositiveButton("非常乐意", new NoDouleDialogClickListener() {
                    @Override
                    protected void onNoDoubleClick(DialogInterface dialog, int which) {
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

    private void noUpdateDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("已经是最新版啦").setMessage(UPDATE_DETAIL).setCancelable(true)
                .setNegativeButton("下次再来", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateDialog(UPDATE_URL);
        }
    };

    private Runnable autoCheckUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UpdateChecker dbP = new UpdateChecker();
                    if (dbP.getConn(opts_t) != null) {
                        int code;
                        if ((code = dbP.codeSelect("select max(code) from app_version")) > getVersionCode(getApplicationContext())) {
                            String res[] = dbP.urlAndDetailSelect("select url, detail from download_url u, details d " +
                                    "where d.code = u.code and d.code = " + code);
                            UPDATE_URL = res[0];
                            if (res[1] == null)
                                res[1] = "";
                            UPDATE_DETAIL = "检测到有新版本哦，快快下载吧！\n" + res[1];
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

    private Runnable noUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            noUpdateDialog();
        }
    };

    private Runnable msgRefreshRunnable = new Runnable() {
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn(opts_o) != null) {
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
                if (curSelectedPosition == 1 && messageFragment != null)
                    messageFragment.getNewMsg();
            }
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

    public boolean getIsSeen() {
        return isSeen;
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

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    private void setMenuItemVisible(boolean feedback, boolean update, boolean search) {
        feedbackMenuItem.setVisible(feedback);
        updateMenuItem.setVisible(update);
        searchMenuItem.setVisible(search);
    }

    public void exitDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("可爱的提示框").setMessage("确定要退出登录切换用户吗亲？").setCancelable(false)
                .setPositiveButton("好哒", new NoDouleDialogClickListener() {
                    @Override
                    protected void onNoDoubleClick(DialogInterface dialog, int which) {
                        DataProcessor dp = new DataProcessor(HomeActivity.this);
                        dp.saveData("uid", "");
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.putExtra("opts_o", opts_o);
                        intent.putExtra("opts_t", opts_t);
                        startActivity(intent);
                        HomeActivity.this.finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        feedbackMenuItem = menu.findItem(R.id.action_feedback);
        feedbackMenuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                Intent intent = new Intent(HomeActivity.this, FeedbackActivity.class);
                intent.putExtra("opts_o", opts_o);
                intent.putExtra("uid", UID);
                startActivity(intent);
            }
        });
        feedbackMenuItem.setVisible(false);
        updateMenuItem = menu.findItem(R.id.action_update);
        updateMenuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                handCheckUpdate();
            }
        });
        updateMenuItem.setVisible(false);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("opts_o", opts_o);
                startActivity(intent);
            }
        });
        searchMenuItem.setVisible(true);
        return true;
    }
}
