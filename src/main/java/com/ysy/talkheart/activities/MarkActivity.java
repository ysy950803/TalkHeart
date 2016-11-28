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
import com.ysy.talkheart.adapters.MeMarkListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MarkActivity extends AppCompatActivity {

    private RecyclerView markRecyclerView;
    private MeMarkListViewAdapter listViewAdapter;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
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

        timeList.add("2016/11/23");
        timeList.add("2016/11/22");
        timeList.add("2016/11/21");

        textList.add(getString(R.string.home_active_text));
        textList.add(getString(R.string.home_active_text_2));
        textList.add(getString(R.string.home_active_text_3));
    }

    private void initView() {
        markRecyclerView = (RecyclerView) findViewById(R.id.me_mark_listView);
        markRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeMarkListViewAdapter(avatarList, nicknameList, timeList, textList);
        markRecyclerView.setAdapter(listViewAdapter);
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
