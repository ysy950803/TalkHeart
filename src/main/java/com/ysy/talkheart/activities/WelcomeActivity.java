package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ActivitiesDestroyer;

public class WelcomeActivity extends AppCompatActivity {

    private static final int WAIT_TIME = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActivitiesDestroyer.getInstance().addActivity(this);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, findViewById(R.id.welcome_logo_img), getString(R.string.trans_logo));
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                }
            }
        }, WAIT_TIME);
    }
}
