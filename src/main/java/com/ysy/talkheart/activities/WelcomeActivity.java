package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightFullScreenActivity;
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.DataProcessor;
import com.ysy.talkheart.utils.StringUtils;

public class WelcomeActivity extends DayNightFullScreenActivity {

    private String UID = "0";
    private String[] org_opts_o = new String[4];
    private String[] org_opts_t = new String[4];

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return "Version " + pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Version For You";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActivitiesDestroyer.getInstance().addActivity(this);
        TextView version = (TextView) findViewById(R.id.welcome_version_tv);
        version.setText(getVersionName(this));
        try {
            String[] temps = initOption();
            org_opts_o[0] = temps[2];
            org_opts_o[1] = temps[3];
            org_opts_o[2] = temps[0];
            org_opts_o[3] = temps[1];
            org_opts_t[0] = temps[2];
            org_opts_t[1] = temps[4];
            org_opts_t[2] = temps[0];
            org_opts_t[3] = temps[1];
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出现了一点小异常，请重启啦", Toast.LENGTH_SHORT).show();
            finish();
        }
        int WAIT_TIME;
        DataProcessor dp = new DataProcessor(WelcomeActivity.this);
        UID = dp.readStrData("uid");
        if (!UID.equals("")) {
            WAIT_TIME = 256;
        } else
            WAIT_TIME = 1024;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!UID.equals("")) {
                    Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                    intent.putExtra("uid", UID);
                    intent.putExtra("opts_o", org_opts_o);
                    intent.putExtra("opts_t", org_opts_t);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    intent.putExtra("opts_o", org_opts_o);
                    intent.putExtra("opts_t", org_opts_t);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, findViewById(R.id.welcome_logo_img), getString(R.string.trans_logo));
                        startActivity(intent, tAO.toBundle());
                    } else {
                        startActivity(intent);
                    }
                }
            }
        }, WAIT_TIME);
    }

    private String[] initOption() throws Exception {
        StringUtils utils = new StringUtils();
        String[] temps = new String[6];
        temps[0] = getResources().getString(R.string.trans_title);
        temps[1] = getResources().getString(R.string.trans_button);
        temps[2] = getResources().getString(R.string.trans_img);
        temps[3] = getResources().getString(R.string.trans_fab);
        temps[4] = getResources().getString(R.string.trans_text_2) + getResources().getString(R.string.trans_text_1);
        temps[5] = getResources().getString(R.string.trans_list_3) + getResources().getString(R.string.trans_list_2)
                + getResources().getString(R.string.trans_list_1);
        return utils.eU(temps);
    }
}
