package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightFullScreenActivity;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.DataProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;

public class LoginActivity extends DayNightFullScreenActivity {

    private ImageView loginImg;
    private EditText userEdt;
    private EditText pwEdt;
    private RelativeLayout registerLayout;
    private View focusView;
    private long exitTime;
    private Handler loginHandler;
    private ProgressDialog waitDialog;
    private String[] opts_t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivitiesDestroyer.getInstance().addActivity(this);
        opts_t = getIntent().getExtras().getStringArray("opts_t");
        initView();
        clickListener();
        loginHandler = new Handler();
    }

    private void initView() {
        loginImg = (ImageView) findViewById(R.id.login_logo_img);
        userEdt = (EditText) findViewById(R.id.login_user_edt);
        pwEdt = (EditText) findViewById(R.id.login_pw_edt);
        registerLayout = (RelativeLayout) findViewById(R.id.login_register_tip_layout);
    }

    private void clickListener() {
        loginImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                String username = userEdt.getText().toString();
                String pw = pwEdt.getText().toString();
                ConnectionDetector cd = new ConnectionDetector(LoginActivity.this);
                if (cd.isConnectingToInternet())
                    login(username, pw);
                else
                    Toast.makeText(LoginActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            }
        });
        registerLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, registerLayout, getString(R.string.trans_register));
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class).putExtra("opts_o", opts_o), tAO.toBundle());
                } else {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class).putExtra("opts_o", opts_o));
                }
            }
        });
    }

    private boolean login(final String username, final String pw) {
        userEdt.setError(null);
        pwEdt.setError(null);
        focusView = null;
        if (username.equals("")) {
            userEdt.setError("要有用户名哦");
            focusView = userEdt;
            focusView.requestFocusFromTouch();
            return false;
        } else if (pw.equals("")) {
            pwEdt.setError("要有密码哦");
            focusView = pwEdt;
            focusView.requestFocusFromTouch();
            return false;
        }
        waitDialog = ProgressDialog.show(LoginActivity.this, "请稍后", "正在开启新世界的大门……");
        connectToLogin(username, pw);
        return true;
    }

    private void connectToLogin(final String username, final String pw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    loginHandler.post(timeOutRunnable);
                else {
                    String[] res = dbP.loginSelect("select uid, pw from user where username = '" + username + "'");
                    if (res == null)
                        loginHandler.post(serverErrorRunnable);
                    else {
                        if (res[0] == null)
                            loginHandler.post(userErrorRunnable);
                        else {
                            if (res[1].equals(pw)) {
                                DataProcessor dp = new DataProcessor(LoginActivity.this);
                                dp.saveData("uid", res[0]);
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.putExtra("opts_o", opts_o);
                                intent.putExtra("opts_t", opts_t);
                                intent.putExtra("uid", res[0]);
                                startActivity(intent);
                            } else
                                loginHandler.post(pwErrorRunnable);
                        }
                    }
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private Runnable userErrorRunnable = new Runnable() {
        @Override
        public void run() {
            userEdt.setError("用户不存在");
            focusView = userEdt;
            focusView.requestFocusFromTouch();
        }
    };

    private Runnable pwErrorRunnable = new Runnable() {
        @Override
        public void run() {
            pwEdt.setError("密码错啦");
            focusView = pwEdt;
            focusView.requestFocusFromTouch();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(LoginActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(LoginActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivitiesDestroyer.getInstance().killAll();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次就能退出啦", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                onBackPressed();
            }
            return false; // 此处返回true达到的效果相同，但若不返回值，会出现按一次就显示提示并直接退出
        }
        return super.onKeyDown(keyCode, event);
    }
}
