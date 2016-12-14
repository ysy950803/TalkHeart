package com.ysy.talkheart.activities;

import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.DBProcessor;

public class PersonActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private FloatingActionButton watchFab;
    private ImageView avatarImg;
    private TextView introTv;
    private LinearLayout activeNumLayout;
    private LinearLayout watchNumLayout;
    private LinearLayout fansNumLayout;
    private TextView activeNumTv;
    private TextView watchNumTv;
    private TextView fansNumTv;
    private TextView schoolTv;
    private TextView birthdayTv;
    private MenuItem modifyMenuItem;

    private Handler personHandler;

    private boolean isFromMe;
    private String E_UID = "0";
    private String UID = "0";
    private String SEX = "1";
    private String NICKNAME = "加载中…";
    private String INTRODUCTION = "加载中…";
    private String ACTIVE_NUM = "0";
    private String WATCH_NUM = "0";
    private String FANS_NUM = "0";

    private String SCHOOL = "加载中…";
    private String BIRTHDAY = "加载中…";

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        setSupportActionBar((Toolbar) findViewById(R.id.person_toolbar));
        setupActionBar();
        personHandler = new Handler();

        initData();
        initView();
        clickListener();
        connectToGetPersonInfo(isFromMe);
    }

    private void initData() {
        E_UID = getIntent().getExtras().getString("e_uid", null);
        isFromMe = E_UID == null;
        // offline data
        UID = getIntent().getExtras().getString("uid");
        SEX = getIntent().getExtras().getString("sex");
        NICKNAME = getIntent().getExtras().getString("nickname");

        if (isFromMe) {
            String introStr = getIntent().getExtras().getString("intro");
            if (introStr == null || introStr.equals("点击设置签名"))
                INTRODUCTION = "未设置签名";
            else
                INTRODUCTION = introStr;

            ACTIVE_NUM = getIntent().getExtras().getString("active_num");
            WATCH_NUM = getIntent().getExtras().getString("watch_num");
            FANS_NUM = getIntent().getExtras().getString("fans_num");
        }
    }

    private void initView() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.person_coordinatorLayout);
        avatarImg = (ImageView) findViewById(R.id.person_avatar_img);
        watchFab = (FloatingActionButton) findViewById(R.id.person_watch_fab);
        activeNumLayout = (LinearLayout) findViewById(R.id.person_active_layout);
        watchNumLayout = (LinearLayout) findViewById(R.id.person_watch_layout);
        fansNumLayout = (LinearLayout) findViewById(R.id.person_fans_layout);
        introTv = (TextView) findViewById(R.id.person_intro_tv);
        activeNumTv = (TextView) findViewById(R.id.person_active_num_tv);
        watchNumTv = (TextView) findViewById(R.id.person_watch_num_tv);
        fansNumTv = (TextView) findViewById(R.id.person_fans_num_tv);
        schoolTv = (TextView) findViewById(R.id.person_school_tv);
        birthdayTv = (TextView) findViewById(R.id.person_birthday_tv);

        actionBar.setTitle(NICKNAME);
        watchFab.setVisibility((isFromMe || E_UID.equals(UID)) ? View.GONE : View.VISIBLE);
        avatarImg.setImageResource(SEX.equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
    }

    private void clickListener() {
        watchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        activeNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        watchNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fansNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setClickable(false);
    }

    private void connectToGetPersonInfo(boolean isFromMe) {
        if (isFromMe) {
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn() == null) {
                        personHandler.post(timeOutRunnable);
                    } else {
                        String[] res = dbP.personInfoSelect(
                                "select school, birthday from user where uid = " + UID, true
                        );
                        if (res[1].equals("/(ㄒoㄒ)/~~")) {
                            personHandler.post(serverErrorRunnable);
                        } else {
                            SCHOOL = res[0] == null ? "未设置院校" : res[0];
                            BIRTHDAY = res[1] == null ? "未设置生日" : res[1];
                            personHandler.post(successRunnable);
                        }
                    }
                    dbP.closeConn();
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn() == null) {
                        personHandler.post(timeOutRunnable);
                    } else {
                        String[] res = dbP.personInfoSelect(
                                "select school, birthday, intro from user where uid = " + UID, false
                        );
                        if (res[1].equals("/(ㄒoㄒ)/~~")) {
                            personHandler.post(serverErrorRunnable);
                        } else {
                            SCHOOL = res[0] == null ? "未设置院校" : res[0];
                            BIRTHDAY = res[1] == null ? "未设置生日" : res[1];
                            INTRODUCTION = res[2] == null ? "未设置签名" : res[2];
                            personHandler.post(successRunnable);
                        }
                    }
                    dbP.closeConn();
                }
            }).start();
        }
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            if (!isFromMe) {
                introTv.setText(INTRODUCTION);
                activeNumTv.setText(ACTIVE_NUM);
                watchNumTv.setText(WATCH_NUM);
                fansNumTv.setText(FANS_NUM);
            }
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "服务器君生病了，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "连接超时啦，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    private void setClickable(boolean isAble) {
        activeNumLayout.setClickable(isAble);
        watchNumLayout.setClickable(isAble);
        fansNumLayout.setClickable(isAble);
    }

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_person, menu);
        modifyMenuItem = menu.findItem(R.id.action_modify);
        modifyMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });
        modifyMenuItem.setVisible(isFromMe || (E_UID.equals(UID)));
        return true;
    }

}
