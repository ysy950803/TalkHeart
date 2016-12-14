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
import com.ysy.talkheart.adapters.MeFansListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FansActivity extends AppCompatActivity {

    private RecyclerView fansRecyclerView;
    private MeFansListViewAdapter listViewAdapter;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> infoList = new ArrayList<>();
    private List<Boolean> eachOtherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fans);
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

        infoList.add(getString(R.string.content_loading));
        infoList.add(getString(R.string.content_loading));
        infoList.add(getString(R.string.content_loading));

        eachOtherList.add(true);
        eachOtherList.add(false);
        eachOtherList.add(true);
    }

    private void initView() {
        fansRecyclerView = (RecyclerView) findViewById(R.id.me_fans_listView);
        fansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeFansListViewAdapter(avatarList, nicknameList, infoList, eachOtherList);
        fansRecyclerView.setAdapter(listViewAdapter);
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
