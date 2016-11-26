package com.ysy.talkheart.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.HomeFragment;
import com.ysy.talkheart.fragments.MeFragment;
import com.ysy.talkheart.fragments.MessageFragment;
import com.ysy.talkheart.utils.ActivitiesDestroyer;

public class HomeActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {

    private HomeFragment homeFragment;
    private MessageFragment messageFragment;
    private MeFragment meFragment;
    int lastSelectedPosition = 0;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        ActivitiesDestroyer.getInstance().killAll();

        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "首页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_message_white_24dp, "消息"))
                .addItem(new BottomNavigationItem(R.drawable.ic_person_pin_white_24dp, "个人"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setActiveColor("#2196F3")
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        if (actionBar != null)
            actionBar.setTitle("首页");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        homeFragment = HomeFragment.newInstance("First", "Home");
        transaction.replace(R.id.content_table_layout, homeFragment);
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = this.getSupportFragmentManager();
        // 开启事务
        FragmentTransaction transaction = fm.beginTransaction();
        switch (position) {
            case 0:
                if (actionBar != null)
                    actionBar.setTitle("首页");
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance("First", "Home");
                }
                transaction.replace(R.id.content_table_layout, homeFragment);
                break;
            case 1:
                if (actionBar != null)
                    actionBar.setTitle("消息");
                if (messageFragment == null) {
                    messageFragment = MessageFragment.newInstance("Second", "Message");
                }
                transaction.replace(R.id.content_table_layout, messageFragment);
                break;
            case 2:
                if (actionBar != null)
                    actionBar.setTitle("个人");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance("Third", "Me");
                }
                transaction.replace(R.id.content_table_layout, meFragment);
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
}
