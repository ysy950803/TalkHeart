package com.ysy.talkheart.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.MeActiveListViewAdapter;
import com.ysy.talkheart.adapters.ListOnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ActiveActivity extends AppCompatActivity {

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<Boolean> goodStatusList = new ArrayList<>();
    private List<Integer> goodNumList = new ArrayList<>();

    private RecyclerView activeRecyclerView;
    private MeActiveListViewAdapter listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        setupActionBar();
        initData();
        initView();
        clickListener();
    }

    private void initData() {
        avatarList.add(R.drawable.me_avatar_boy);
        avatarList.add(R.drawable.me_avatar_girl);
        avatarList.add(R.drawable.me_avatar_boy);

        nicknameList.add("原子君");
        nicknameList.add("分子君");
        nicknameList.add("质子君");

        timeList.add("2分钟前");
        timeList.add("4分钟前");
        timeList.add("8分钟前");

        textList.add(getString(R.string.home_active_text));
        textList.add(getString(R.string.home_active_text_2));
        textList.add(getString(R.string.home_active_text_3));

        goodStatusList.add(true);
        goodStatusList.add(false);
        goodStatusList.add(true);

        goodNumList.add(8);
        goodNumList.add(96);
        goodNumList.add(192);
    }

    private void initView() {
        activeRecyclerView = (RecyclerView) findViewById(R.id.me_active_listView);
        activeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeActiveListViewAdapter(avatarList, nicknameList, timeList, textList, goodStatusList, goodNumList);
        activeRecyclerView.setAdapter(listViewAdapter);
    }

    private void clickListener() {
        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
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
}
