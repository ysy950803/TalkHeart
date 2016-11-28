package com.ysy.talkheart.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.adapters.MeWatchListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class WatchActivity extends AppCompatActivity {

    private RecyclerView watchRecyclerView;
    private MeWatchListViewAdapter listViewAdapter;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> infoList = new ArrayList<>();
    private List<Integer> relationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
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

        infoList.add(getString(R.string.me_introduction));
        infoList.add(getString(R.string.me_introduction));
        infoList.add(getString(R.string.me_introduction));

        relationList.add(1);
        relationList.add(0);
        relationList.add(1);
    }

    private void initView() {
        watchRecyclerView = (RecyclerView) findViewById(R.id.me_watch_listView);
        watchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeWatchListViewAdapter(avatarList, nicknameList, infoList, relationList);
        watchRecyclerView.setAdapter(listViewAdapter);
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
