package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.DataProcessor;

public class WelcomeActivity extends AppCompatActivity {

    private String UID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActivitiesDestroyer.getInstance().addActivity(this);
        int WAIT_TIME;
        DataProcessor dp = new DataProcessor(WelcomeActivity.this);
        UID = dp.readStrData("uid");
        if (!UID.equals("")) {
            WAIT_TIME = 512;
        } else
            WAIT_TIME = 2048;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!UID.equals("")) {
                    Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                    intent.putExtra("uid", UID);
                    startActivity(intent);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, findViewById(R.id.welcome_logo_img), getString(R.string.trans_logo));
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class), tAO.toBundle());
                    } else {
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                    }
                }
            }
        }, WAIT_TIME);
    }
}
