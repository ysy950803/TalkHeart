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
import com.ysy.talkheart.adapters.MeDraftListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DraftActivity extends AppCompatActivity {

    private RecyclerView draftRecyclerView;
    private MeDraftListViewAdapter listViewAdapter;

    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        setupActionBar();
        initData();
        initView();
        clickListener();
    }

    private void initData() {
        timeList.add(getString(R.string.me_draft_time));
        textList.add(getString(R.string.me_draft_text));
    }

    private void initView() {
        draftRecyclerView = (RecyclerView) findViewById(R.id.me_draft_listView);
        draftRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeDraftListViewAdapter(timeList, textList);
        draftRecyclerView.setAdapter(listViewAdapter);
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
