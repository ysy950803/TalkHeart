package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ActivitiesDestroyer;

public class LoginActivity extends AppCompatActivity {

    private ImageView loginImg;
    private EditText userEdt;
    private EditText pwEdt;
    private RelativeLayout registerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivitiesDestroyer.getInstance().addActivity(this);
        initView();
        clickListener();
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
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                ActivitiesDestroyer.getInstance().killAll();
            }
        });
        registerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, registerLayout, getString(R.string.trans_register));
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivitiesDestroyer.getInstance().killAll();
    }
}
