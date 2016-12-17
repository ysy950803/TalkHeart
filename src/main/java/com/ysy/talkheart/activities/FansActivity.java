package com.ysy.talkheart.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.adapters.MeFansListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FansActivity extends AppCompatActivity {

    private MeFansListViewAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private Handler fansHandler;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> introList = new ArrayList<>();
    private List<Integer> relationList = new ArrayList<>();
    private List<String> fansUIDList = new ArrayList<>();

    private String UID = "0";
    private String E_UID = "0";
    public ImageView eachOtherImg;
    private boolean isSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fans);
        setupActionBar();
        fansHandler = new Handler();
        initData();
        initView();
        clickListener();

        refresh();
    }

    private void initData() {
        E_UID = getIntent().getExtras().getString("e_uid");
        UID = getIntent().getExtras().getString("uid");
        isSelf = E_UID.equals(UID);
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.me_fans_refresh_layout);
        RecyclerView fansRecyclerView = (RecyclerView) findViewById(R.id.me_fans_listView);
        fansRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        listViewAdapter = new MeFansListViewAdapter(this, avatarList, nicknameList, introList, relationList, !isSelf);
        fansRecyclerView.setAdapter(listViewAdapter);

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void clickListener() {
        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ConnectionDetector cd = new ConnectionDetector(FansActivity.this);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(FansActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(FansActivity.this, PersonActivity.class);
                    intent.putExtra("uid", fansUIDList.get(position));
                    intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
                    intent.putExtra("nickname", nicknameList.get(position));
                    intent.putExtra("e_uid", E_UID);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void refresh() {
        if (!isRefreshing) {
            isRefreshing = true;
            if (!refreshData()) {
                refreshLayout.setRefreshing(false);
                isRefreshing = false;
            }
        }
    }

    private boolean refreshData() {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToGetFans(UID);
        return true;
    }

    private void connectToGetFans(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    fansHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.fansSelect(
                            "select u.uid, sex, nickname, intro, relation from user u, user_relation ur " +
                                    "where ur.uid_a = " + uid + " and ur.uid_b = u.uid",
                            "select u.uid, sex, nickname, intro, relation from user u, user_relation ur " +
                                    "where ur.uid_b = " + uid + " and ur.uid_a = u.uid"
                    );
                    clearAllLists();
                    if (resList == null) {
                        fansHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        fansHandler.post(nothingListRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            fansUIDList.add(resList.get(0).get(i));
                            avatarList.add(resList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(resList.get(2).get(i));
                            introList.add(resList.get(3).get(i));
                            relationList.add(Integer.parseInt(resList.get(4).get(i)));
                        }
                        fansHandler.post(successRunnable);
                    }
                }
            }
        }).start();
    }

    public void updateRelation(int position, int relation) {
        connectToUpdateRelation(UID, fansUIDList.get(position), relation);
    }

    private void connectToUpdateRelation(final String uid_a, final String uid_b, final int relation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    fansHandler.post(timeOutRunnable);
                } else {
                    switch (relation) {
                        case -1: // 2 to -1
                            int res = dbP.update("update user_relation set uid_a = " + uid_a + ", " +
                                    "uid_b = " + uid_b + ", relation = -1 where (uid_a = " + uid_a + " and uid_b = " + uid_b + ") or (" +
                                    "uid_a = " + uid_b + " and uid_b = " + uid_a + ")");
                            if (res == 1) {
                                dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid_a);
                                dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + uid_b);
                                fansHandler.post(unWatchRunnable);
                            } else
                                fansHandler.post(serverErrorRunnable);
                            break;
                        case 2: // -1 to 2
                            int res2 = dbP.update("update user_relation set uid_a = " + uid_a + ", " +
                                    "uid_b = " + uid_b + ", relation = 2 where (uid_a = " + uid_a + " and uid_b = " + uid_b + ") or (" +
                                    "uid_a = " + uid_b + " and uid_b = " + uid_a + ")");
                            if (res2 == 1) {
                                dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid_a);
                                dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + uid_b);
                                fansHandler.post(eachRunnable);
                            } else
                                fansHandler.post(serverErrorRunnable);
                            break;
                    }
                }
                if (eachOtherImg != null) {
                    eachOtherImg.setClickable(true);
                    eachOtherImg = null;
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        introList.clear();
        relationList.clear();
        fansUIDList.clear();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FansActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FansActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable nothingListRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(FansActivity.this, "还没有任何粉丝哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable unWatchRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FansActivity.this, "成功取消关注", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable eachRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FansActivity.this, "你们俩互相关注啦", Toast.LENGTH_SHORT).show();
        }
    };

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
