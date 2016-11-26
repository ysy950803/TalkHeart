package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;

public class LoginActivity extends AppCompatActivity {

    private ImageView loginImg;
    private EditText userEdt;
    private EditText pwEdt;
    private RelativeLayout registerLayout;
    private View focusView;

    private Handler loginHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivitiesDestroyer.getInstance().addActivity(this);
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
        loginImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userEdt.getText().toString();
                String pw = pwEdt.getText().toString();
                ConnectionDetector cd = new ConnectionDetector(LoginActivity.this);
                if (cd.isConnectingToInternet())
                    login(username, pw);
                else
                    Toast.makeText(LoginActivity.this, "请检查网络连接哦！", Toast.LENGTH_SHORT).show();
            }
        });
        registerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, registerLayout, getString(R.string.trans_register));
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            }
        });
    }

//    private Handler loginHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
//                    userEdt.setError("用户不存在");
//                    focusView = userEdt;
//                    focusView.requestFocusFromTouch();
//                    break;
//                case 1:
//                    pwEdt.setError("密码错误");
//                    focusView = pwEdt;
//                    focusView.requestFocusFromTouch();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    private boolean login(final String username, final String pw) {
        userEdt.setError(null);
        pwEdt.setError(null);
        focusView = null;
        if (username.equals("")) {
            userEdt.setError("用户名不能为空");
            focusView = userEdt;
            focusView.requestFocusFromTouch();
            return false;
        } else if (pw.equals("")) {
            pwEdt.setError("密码不能为空");
            focusView = pwEdt;
            focusView.requestFocusFromTouch();
            return false;
        }
        connectToLogin(username, pw);
        return true;
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
            pwEdt.setError("密码错误");
            focusView = pwEdt;
            focusView.requestFocusFromTouch();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(LoginActivity.this, "服务器连接出错，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private void connectToLogin(final String username, final String pw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                dbP.getConn();
                String res = dbP.loginSelect("select pw from user where username = '" + username + "'");
                if (res.equals(pw)) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                            loginHandler = null;
                } else if (res.equals("用户不存在")) {
//                            loginHandler.sendEmptyMessage(0);
                    loginHandler.post(userErrorRunnable);
                } else if (res.equals("")) {
                    loginHandler.post(serverErrorRunnable);
                } else {
//                            loginHandler.sendEmptyMessage(1);
                    loginHandler.post(pwErrorRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivitiesDestroyer.getInstance().killAll();
    }
}
