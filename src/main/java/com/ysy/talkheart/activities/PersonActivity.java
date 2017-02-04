package com.ysy.talkheart.activities;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightNoActionBarActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;
import com.ysy.talkheart.utils.NoDouleDialogClickListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class PersonActivity extends DayNightNoActionBarActivity {

    private static final String IMAGE_UNSPECIFIED = "image/*";
    private static final int ALBUM_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int CROP_REQUEST_CODE = 4;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 3;
    private String AVATAR_UPLOAD_URL = "";
    private long AVATAR_TIME_POINT = System.currentTimeMillis();

    private CollapsingToolbarLayout toolbarLayout;
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
    private String[] opts_o;
    private boolean isCanClick = false;
    private ProgressDialog waitDialog;
    private byte[] avatarBytes;

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
        if (avatarBytes == null)
            downloadAvatar();
        connectToGetPersonInfo();
    }

    @Override
    protected void onResume() {
        if (((GlobalApp) getApplication()).getMeInfoUpdated())
            connectToGetPersonInfo();
        super.onResume();
    }

    private void initData() {
        AVATAR_UPLOAD_URL = getResources().getString(R.string.url_avatar_upload);
        avatarBytes = getIntent().getExtras().getByteArray("avatar");
        opts_o = getIntent().getExtras().getStringArray("opts_o");
        E_UID = getIntent().getExtras().getString("e_uid");
        UID = getIntent().getExtras().getString("uid");
        SEX = getIntent().getExtras().getString("sex", "1");
        UID_L = Integer.parseInt(E_UID) < Integer.parseInt(UID) ? E_UID : UID;
        UID_H = Integer.parseInt(E_UID) > Integer.parseInt(UID) ? E_UID : UID;
        isSelf = E_UID.equals(UID);
        isEmptyRelation = true;
    }

    private void initView() {
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.person_toolbar_layout);
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
        watchFab.setVisibility(isSelf ? View.GONE : View.VISIBLE);

        if (avatarBytes != null)
            avatarImg.setImageBitmap(BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length));
    }

    private void clickListener() {
        avatarImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (isSelf) {
                    String items[] = {"选张美图", "来拍个照"};
                    showItemDialog(items);
                }
            }
        });

        watchFab.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                watchHimOrHer();
            }
        });

        activeNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(PersonActivity.this, ActiveActivity.class);
                if (avatarBytes != null) {
                    intent.putExtra("avatar", avatarBytes);
                }
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, activeNumTv, getString(R.string.trans_active));
                    startActivity(intent, tAO.toBundle());
                } else
                    startActivity(intent);
            }
        });

        watchNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(PersonActivity.this, WatchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, watchNumTv, getString(R.string.trans_watch));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        fansNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(PersonActivity.this, FansActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", E_UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(PersonActivity.this, fansNumTv, getString(R.string.trans_fans));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });
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
        watchFab.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    personHandler.post(timeOutFabRunnable);
                } else {
                    String[] relation = dbP.relationSelect(
                            "select uid_a, uid_b, relation from user_relation where uid_a = " + UID_L + " and uid_b = " + UID_H
                    );
                    if (relation[0] != null && relation[0].equals("-2"))
                        personHandler.post(serverErrorRunnable);
                    else {
                        if (relation[0] == null)
                            isEmptyRelation = true;
                        else {
                            isEmptyRelation = false;
                            RELATION = relation[2];
                        }
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
                                            personHandler.post(serverErrorFabRunnable);
                                    } else {
                                        RELATION = "2";
                                        int res = dbP.update("update user_relation set relation = " + RELATION +
                                                " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                            personHandler.post(eachRunnable);
                                        } else
                                            personHandler.post(serverErrorFabRunnable);
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
                                            personHandler.post(serverErrorFabRunnable);
                                    } else {
                                        RELATION = "0";
                                        int res1 = dbP.update("update user_relation set relation = " + RELATION +
                                                " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res1 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_h);
                                            dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_l);
                                            personHandler.post(nothingRunnable);
                                        } else
                                            personHandler.post(serverErrorFabRunnable);
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
                                            personHandler.post(serverErrorFabRunnable);
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
                                            personHandler.post(serverErrorFabRunnable);
                                    } else {
                                        RELATION = "-1";
                                        int res3 = dbP.update("update user_relation set relation = " + RELATION +
                                                " where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res3 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                            personHandler.post(watchRunnable);
                                        } else
                                            personHandler.post(serverErrorFabRunnable);
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
                                    personHandler.post(serverErrorFabRunnable);
                            } else {
                                int res = dbP.insert("insert into user_relation values(" + uid_l + ", " + uid_h + ", -1)");
                                if (res == 1) {
                                    isEmptyRelation = false;
                                    RELATION = "-1";
                                    dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_h);
                                    dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_l);
                                    personHandler.post(watchRunnable);
                                } else
                                    personHandler.post(serverErrorFabRunnable);
                            }
                        }
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToGetPersonInfo() {
        setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    personHandler.post(timeOutRunnable);
                } else {
                    String[] res = dbP.personInfoSelect(
                            "select sex, nickname, school, birthday, intro, act_num, watch_num, fans_num " +
                                    "from user u, user_info_count uic where u.uid = " + UID + " and u.uid = uic.uid"
                    );
                    if (res[1].equals("/(ㄒoㄒ)/~~")) {
                        personHandler.post(serverErrorRunnable);
                    } else {
                        SEX = res[0];
                        NICKNAME = res[1];
                        SCHOOL = res[2] == null ? "未设置院校" : res[2];
                        BIRTHDAY = res[3] == null ? "未设置生日" : res[3];
                        INTRODUCTION = res[4] == null ? "未设置签名" : res[4];
                        ACTIVE_NUM = res[5];
                        WATCH_NUM = res[6];
                        FANS_NUM = res[7];

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
            connectToGetPersonInfo();
        }
    };

    private Runnable unWatchRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
            Toast.makeText(PersonActivity.this, "成功取消关注", Toast.LENGTH_SHORT).show();
            connectToGetPersonInfo();
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_nothing_blue_36dp);
            Toast.makeText(PersonActivity.this, "你们俩不再有关系啦", Toast.LENGTH_SHORT).show();
            connectToGetPersonInfo();
        }
    };

    private Runnable eachRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_each_other_pink_36dp);
            Toast.makeText(PersonActivity.this, "你们俩互相关注啦", Toast.LENGTH_SHORT).show();
            connectToGetPersonInfo();
        }
    };

    private Runnable watchShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_watch_blue_pink_36dp);
            refreshView();
        }
    };

    private Runnable unWatchShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
            refreshView();
        }
    };

    private Runnable nothingShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_nothing_blue_36dp);
            refreshView();
        }
    };

    private Runnable successShowRunnable = new Runnable() {
        @Override
        public void run() {
            refreshView();
        }
    };

    private Runnable eachShowRunnable = new Runnable() {
        @Override
        public void run() {
            watchFab.setImageResource(R.mipmap.ic_each_other_pink_36dp);
            refreshView();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "服务器君生病了，返回重试吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable serverErrorFabRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "服务器君生病了，请重试", Toast.LENGTH_SHORT).show();
            watchFab.setClickable(true);
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "连接超时啦，返回重试吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutFabRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            watchFab.setClickable(true);
        }
    };

    private void refreshView() {
        if (avatarBytes == null)
            avatarImg.setImageResource(SEX.equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
        toolbarLayout.setTitle(NICKNAME);
        schoolTv.setText(SCHOOL);
        birthdayTv.setText(BIRTHDAY);
        introTv.setText(INTRODUCTION);
        activeNumTv.setText(ACTIVE_NUM);
        watchNumTv.setText(WATCH_NUM);
        fansNumTv.setText(FANS_NUM);
        setClickable(true);
    }

    private void setClickable(boolean isAble) {
        isCanClick = isAble;
        if (watchFab.getVisibility() == View.VISIBLE)
            watchFab.setClickable(isAble);
        activeNumLayout.setClickable(isAble);
        watchNumLayout.setClickable(isAble);
        fansNumLayout.setClickable(isAble);
    }

    private void setAvatarClickable(boolean isAble) {
        avatarImg.setClickable(isAble);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_person, menu);
        MenuItem modifyMenuItem = menu.findItem(R.id.action_modify);
        modifyMenuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                if (isCanClick) {
                    Intent intent = new Intent(PersonActivity.this, PersonModifyActivity.class);
                    intent.putExtra("uid", E_UID);
                    intent.putExtra("school", SCHOOL.equals("未设置院校") ? "" : SCHOOL);
                    intent.putExtra("nickname", NICKNAME);
                    intent.putExtra("sex", Integer.parseInt(SEX));
                    intent.putExtra("birthday", BIRTHDAY);
                    intent.putExtra("opts_o", opts_o);
                    startActivity(intent);
                } else
                    Toast.makeText(PersonActivity.this, "请稍等，正在加载哦", Toast.LENGTH_SHORT).show();
            }
        });
        modifyMenuItem.setVisible(isSelf);
        return true;
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

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void downloadAvatar() {
        setAvatarClickable(false);
        new AsyncHttpClient().get(AVATAR_UPLOAD_URL + "/" + UID + "_avatar_img.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Bitmap picBmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                        if (picBmp != null) {
                            avatarBytes = responseBody;
                            avatarImg.setImageBitmap(picBmp);
                        } else
                            avatarBytes = null;
                        setAvatarClickable(true);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        avatarBytes = null;
                        setAvatarClickable(true);
                    }
                });
    }

    private void uploadAvatar(final Bitmap picBmp, byte[] avatarBytes, byte[] avatarBytesThumb) {
        if (avatarBytes == null)
            Toast.makeText(this, "头像未更改哦", Toast.LENGTH_SHORT).show();
        else {
            RequestParams params = new RequestParams();
            params.put("avatar_img", new ByteArrayInputStream(avatarBytes), UID + "_avatar_img.jpg", "multipart/form-data");
            params.put("avatar_img_thumb", new ByteArrayInputStream(avatarBytesThumb), UID + "_avatar_img_thumb.jpg", "multipart/form-data");
            new AsyncHttpClient().post(AVATAR_UPLOAD_URL, params, new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    waitDialog = ProgressDialog.show(PersonActivity.this, "正在上传头像", "请稍等…");
                    super.onStart();
                }

                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    waitDialog.dismiss();
                    Toast.makeText(PersonActivity.this, "上传头像失败，稍后重试吧", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    waitDialog.dismiss();
                    if (s.equals("Success")) {
                        avatarImg.setImageBitmap(picBmp);
                        ((GlobalApp) getApplication()).setMeInfoUpdated(true);
                        Toast.makeText(PersonActivity.this, "上传头像成功咯", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(PersonActivity.this, "上传头像失败，稍后重试吧", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    boolean isFolderExists(String strFolder) {
        File dir = new File(strFolder);
        return dir.exists() || dir.mkdirs(); // always true
    }

    private void selectImg() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        startActivityForResult(intent, ALBUM_REQUEST_CODE);
    }

    private void takePhoto() {
        if (isFolderExists(Environment.getExternalStorageDirectory() + "/TalkHeart/")) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory()
                    + "/TalkHeart/" + AVATAR_TIME_POINT + "_avatar_img_tmp.jpg")));
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    private void startCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP"); // 调用Android系统自带的一个图片剪裁页面
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true"); // 进行修剪
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 96);
        intent.putExtra("outputY", 96);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    private void requestAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请赐予我权限吧", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        } else {
            takePhoto();
        }
    }

    private void showItemDialog(String[] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new NoDouleDialogClickListener() {
            @Override
            protected void onNoDoubleClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectImg();
                        break;
                    case 1:
                        requestAndTakePhoto();
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "请赐予我权限吧", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            return;
        }
        switch (requestCode) {
            case ALBUM_REQUEST_CODE:
                if (data == null) {
                    return;
                }
                startCrop(data.getData());
                break;
            case CAMERA_REQUEST_CODE:
                File tempPic = new File(Environment.getExternalStorageDirectory()
                        + "/TalkHeart/" + AVATAR_TIME_POINT + "_avatar_img_tmp.jpg");
                startCrop(Uri.fromFile(tempPic));
                break;
            case CROP_REQUEST_CODE:
                if (data == null) {
                    // 如果有则显示之前设置的图片，否则显示默认的图片
                    return;
                }
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap picBmp = extras.getParcelable("data");
                    if (picBmp != null) {
                        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                        picBmp.compress(Bitmap.CompressFormat.JPEG, 100, bAOS); // 百分比（0-100）压缩文件
                        byte[] avatarBytes = bAOS.toByteArray();

                        bAOS = new ByteArrayOutputStream();
                        picBmp.compress(Bitmap.CompressFormat.JPEG, 75, bAOS); // 百分比（0-100）压缩文件
                        byte[] avatarBytesThumb = bAOS.toByteArray();

                        bAOS = new ByteArrayOutputStream();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        BitmapFactory.decodeByteArray(avatarBytesThumb,
                                0, avatarBytesThumb.length, options).compress(Bitmap.CompressFormat.JPEG, 50, bAOS);
                        avatarBytesThumb = bAOS.toByteArray();

                        try {
                            bAOS.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        uploadAvatar(picBmp, avatarBytes, avatarBytesThumb);
                    } else {
                        Toast.makeText(this, "出现未知错误啦，请重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
