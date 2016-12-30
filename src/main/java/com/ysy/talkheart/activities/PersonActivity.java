package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
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
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;

public class PersonActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private FloatingActionButton watchFab;
    private TextView introTv;
    private LinearLayout activeNumLayout;
    private LinearLayout watchNumLayout;
    private LinearLayout fansNumLayout;
    private TextView activeNumTv;
    private TextView watchNumTv;
    private TextView fansNumTv;
    private TextView schoolTv;
    private TextView birthdayTv;

    private Handler personHandler;

    private boolean isSelf;
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

    private boolean isEmptyRelation = true;
    private String RELATION = "-2";
    private String UID_L = "0";
    private String UID_H = "0";

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
        connectToGetPersonInfo();
    }

    private void initData() {
        E_UID = getIntent().getExtras().getString("e_uid");
        UID = getIntent().getExtras().getString("uid");
        SEX = getIntent().getExtras().getString("sex");
        NICKNAME = getIntent().getExtras().getString("nickname");
        UID_L = Integer.parseInt(E_UID) < Integer.parseInt(UID) ? E_UID : UID;
        UID_H = Integer.parseInt(E_UID) > Integer.parseInt(UID) ? E_UID : UID;
        isSelf = E_UID.equals(UID);
    }

    private void initView() {
        ImageView avatarImg = (ImageView) findViewById(R.id.person_avatar_img);
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
        watchFab.setVisibility(isSelf ? View.GONE : View.VISIBLE);
        avatarImg.setImageResource(SEX.equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
    }

    private void clickListener() {
        watchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchHimOrHer();
            }
        });

        activeNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonActivity.this, ActiveActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, activeNumTv, getString(R.string.trans_active));
                    startActivity(intent, tAO.toBundle());
                } else
                    startActivity(intent);
            }
        });

        watchNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonActivity.this, WatchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, watchNumTv, getString(R.string.trans_watch));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        fansNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonActivity.this, FansActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, fansNumTv, getString(R.string.trans_fans));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        setClickable(false);
    }

    private boolean watchHimOrHer() {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToWatch(UID_L, UID_H);
        return true;
    }

    private void connectToWatch(final String uid_l, final String uid_h) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    personHandler.post(timeOutRunnable);
                } else {
                    if (!isEmptyRelation) {
                        switch (RELATION) {
                            case "1":
                                if (uid_l.equals(E_UID)) {
                                    RELATION = "0";
                                    int res = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_l);
                                        dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_h);
                                        personHandler.post(nothingRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                } else {
                                    RELATION = "2";
                                    int res = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                        dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                        personHandler.post(eachRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                }
                                break;
                            case "-1":
                                if (uid_l.equals(E_UID)) {
                                    RELATION = "2";
                                    int res1 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res1 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_l);
                                        dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_h);
                                        personHandler.post(eachRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                } else {
                                    RELATION = "0";
                                    int res1 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res1 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_h);
                                        dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_l);
                                        personHandler.post(nothingRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                }
                                break;
                            case "2":
                                if (uid_l.equals(E_UID)) {
                                    RELATION = "-1";
                                    int res2 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res2 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_l);
                                        dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_h);
                                        personHandler.post(unWatchRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                } else {
                                    RELATION = "1";
                                    int res2 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res2 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_h);
                                        dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_l);
                                        personHandler.post(unWatchRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                }
                                break;
                            case "0":
                                if (uid_l.equals(E_UID)) {
                                    RELATION = "1";
                                    int res3 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res3 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_l);
                                        dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_h);
                                        personHandler.post(watchRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                } else {
                                    RELATION = "-1";
                                    int res3 = dbP.update("update user_relation set relation = " + RELATION +
                                            " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                    if (res3 == 1) {
                                        dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                        dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                        personHandler.post(watchRunnable);
                                    } else
                                        personHandler.post(serverErrorRunnable);
                                }
                                break;
                        }
                    } else {
                        if (uid_l.equals(E_UID)) {
                            int res = dbP.insert("insert into user_relation values(" + uid_l + ", " + uid_h + ", 1)");
                            if (res == 1) {
                                isEmptyRelation = false;
                                RELATION = "1";
                                dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_l);
                                dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_h);
                                personHandler.post(watchRunnable);
                            } else
                                personHandler.post(serverErrorRunnable);
                        } else {
                            int res = dbP.insert("insert into user_relation values(" + uid_l + ", " + uid_h + ", -1)");
                            if (res == 1) {
                                isEmptyRelation = false;
                                RELATION = "1";
                                dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                personHandler.post(watchRunnable);
                            } else
                                personHandler.post(serverErrorRunnable);
                        }
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToGetPersonInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    personHandler.post(timeOutRunnable);
                } else {
                    String[] res = dbP.personInfoSelect(
                            "select school, birthday, intro, act_num, watch_num, fans_num " +
                                    "from user u, user_info_count uic where u.uid = " + UID + " and u.uid = uic.uid"
                    );
                    if (res[1].equals("/(ㄒoㄒ)/~~")) {
                        personHandler.post(serverErrorRunnable);
                    } else {
                        SCHOOL = res[0] == null ? "未设置院校" : res[0];
                        BIRTHDAY = res[1] == null ? "未设置生日" : res[1];
                        INTRODUCTION = res[2] == null ? "未设置签名" : res[2];
                        ACTIVE_NUM = res[3];
                        WATCH_NUM = res[4];
                        FANS_NUM = res[5];

                        if (isSelf)
                            personHandler.post(successShowRunnable);
                        else {
                            String[] relation = dbP.relationSelect(
                                    "select uid_a, uid_b, relation from user_relation where uid_a = " + UID_L + " and uid_b = " + UID_H
                            );
                            if (relation[0] == null) {
                                isEmptyRelation = true;
                                personHandler.post(nothingShowRunnable);
                            } else if (relation[0].equals("-2"))
                                personHandler.post(serverErrorRunnable);
                            else {
                                isEmptyRelation = false;
                                RELATION = relation[2];
                                switch (RELATION) {
                                    case "1":
                                        personHandler.post(UID_L.equals(E_UID) ? watchShowRunnable : unWatchShowRunnable);
                                        break;
                                    case "-1":
                                        personHandler.post(UID_L.equals(E_UID) ? unWatchShowRunnable : watchShowRunnable);
                                        break;
                                    case "2":
                                        personHandler.post(eachShowRunnable);
                                        break;
                                    case "0":
                                        personHandler.post(nothingShowRunnable);
                                        break;
                                }
                            }
                        }
                    }
                }
                dbP.closeConn();
            }
        }).start();

    }

    private Runnable watchRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_watch_blue_pink_36dp);
            Toast.makeText(PersonActivity.this, "成功关注Ta", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable unWatchRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
            Toast.makeText(PersonActivity.this, "成功取消关注", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_nothing_blue_36dp);
            Toast.makeText(PersonActivity.this, "你们俩不再有关系啦", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable eachRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_each_other_pink_36dp);
            Toast.makeText(PersonActivity.this, "你们俩互相关注啦", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable watchShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_watch_blue_pink_36dp);
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
        }
    };

    private Runnable unWatchShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
        }
    };

    private Runnable nothingShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_nothing_blue_36dp);
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
        }
    };

    private Runnable successShowRunnable = new Runnable() {
        @Override
        public void run() {
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
        }
    };

    private Runnable eachShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_each_other_pink_36dp);
            schoolTv.setText(SCHOOL);
            birthdayTv.setText(BIRTHDAY);
            introTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "服务器君生病了，返回重试吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "连接超时啦，返回重试吧", Toast.LENGTH_SHORT).show();
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
        MenuItem modifyMenuItem = menu.findItem(R.id.action_modify);
        modifyMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(PersonActivity.this, ModifyActivity.class);
                intent.putExtra("uid", E_UID);
                intent.putExtra("school", SCHOOL.equals("未设置院校") ? "" : SCHOOL);
                intent.putExtra("nickname", NICKNAME);
                intent.putExtra("sex", Integer.parseInt(SEX));
                intent.putExtra("birthday", BIRTHDAY);
                startActivity(intent);
                return true;
            }
        });
        modifyMenuItem.setVisible(isSelf);
        return true;
    }

}
