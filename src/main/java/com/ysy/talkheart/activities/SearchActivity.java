package com.ysy.talkheart.activities;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.SearchUserListViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEdt;
    private ImageView searchImg;
    private SwipeRefreshLayout refreshLayout;

    private SearchUserListViewAdapter listViewAdapter;
    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> infoList = new ArrayList<>();

    private Handler searchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.search_toolbar));
        setupActionBar();
        initView();
        setSearchContent();
        clickListener();

        searchHandler = new Handler();
    }

    private void initView() {
        searchEdt = (EditText) findViewById(R.id.search_edt);
        searchImg = (ImageView) findViewById(R.id.search_img);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_user_refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    private void clickListener() {
        searchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser(searchEdt.getText().toString());
            }
        });

        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void setSearchContent() {
        RecyclerView searchRecyclerView = (RecyclerView) findViewById(R.id.search_recyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new SearchUserListViewAdapter(avatarList, nicknameList, infoList);
        searchRecyclerView.setAdapter(listViewAdapter);
    }

    private void searchUser(String nicknameLike) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
        } else {
            if (nicknameLike.equals("")) {
                Toast.makeText(this, "Ta叫什么名字呢？", Toast.LENGTH_SHORT).show();
            } else {
                refreshLayout.setRefreshing(true);
                connectToSearchUser(nicknameLike);
            }
        }
    }

    private void connectToSearchUser(final String nicknameLike) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    searchHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.searchUserSelect(
                            "select nickname, sex, intro from user where nickname like '%" + nicknameLike + "%'"
                    );
                    clearAllLists();
                    if (resList == null) {
                        searchHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        searchHandler.post(nothingRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            nicknameList.add(resList.get(0).get(i));
                            avatarList.add(resList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            infoList.add(resList.get(2).get(i));
                        }
                        searchHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            refreshLayout.setRefreshing(false);
            listViewAdapter.notifyDataSetChanged();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            refreshLayout.setRefreshing(false);
            Toast.makeText(SearchActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            refreshLayout.setRefreshing(false);
            Toast.makeText(SearchActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshLayout.setRefreshing(false);
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(SearchActivity.this, "没有找到那个Ta", Toast.LENGTH_SHORT).show();
        }
    };

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        infoList.clear();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
}
