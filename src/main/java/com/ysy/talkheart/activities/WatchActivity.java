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
import com.ysy.talkheart.adapters.MeWatchListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class WatchActivity extends AppCompatActivity {

    private MeWatchListViewAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private Handler watchHandler;
    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> introList = new ArrayList<>();
    private List<Integer> relationList = new ArrayList<>();
    private List<String> watchUIDList = new ArrayList<>();
    private String UID = "0";
    private String E_UID = "0";
    public ImageView eachOtherImg;
    private boolean isSelf;
    private String[] opts_o;
    private RecyclerView watchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        setupActionBar();
        watchHandler = new Handler();
        initData();
        initView();
        clickListener();
    }

    @Override
    protected void onResume() {
        refreshLayout.setRefreshing(true);
        refresh();
        super.onResume();
    }

    private void initData() {
        opts_o = getIntent().getExtras().getStringArray("opts_o");
        E_UID = getIntent().getExtras().getString("e_uid");
        UID = getIntent().getExtras().getString("uid");
        isSelf = E_UID.equals(UID);
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.me_watch_refresh_layout);
        watchRecyclerView = (RecyclerView) findViewById(R.id.me_watch_listView);

        watchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeWatchListViewAdapter(this, avatarList, nicknameList, introList, relationList, !isSelf);
        watchRecyclerView.setAdapter(listViewAdapter);

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
                ConnectionDetector cd = new ConnectionDetector(WatchActivity.this);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(WatchActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(WatchActivity.this, PersonActivity.class);
                    intent.putExtra("uid", watchUIDList.get(position));
                    intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
                    intent.putExtra("nickname", nicknameList.get(position));
                    intent.putExtra("e_uid", E_UID);
                    intent.putExtra("opts_o", opts_o);
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
        connectToGetWatch(UID);
        return true;
    }

    private void connectToGetWatch(final String uid) {
        watchRecyclerView.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    watchHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.watchSelect(
                            "select u.uid, sex, nickname, intro, relation from user u, user_relation ur " +
                                    "where ur.uid_a = " + uid + " and ur.uid_b = u.uid",
                            "select u.uid, sex, nickname, intro, relation from user u, user_relation ur " +
                                    "where ur.uid_b = " + uid + " and ur.uid_a = u.uid"
                    );
                    clearAllLists();
                    if (resList == null) {
                        watchHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        watchHandler.post(nothingListRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            watchUIDList.add(resList.get(0).get(i));
                            avatarList.add(resList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(resList.get(2).get(i));
                            introList.add(resList.get(3).get(i) == null ? "未设置签名" : resList.get(3).get(i));
                            relationList.add(Integer.parseInt(resList.get(4).get(i)));
                        }
                        watchHandler.post(successRunnable);
                    }
                }
                watchRecyclerView.setClickable(true);
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToUpdateRelation(final String uid, final String watch_uid, final String uid_l, final String uid_h) {
        if (eachOtherImg != null)
            eachOtherImg.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    watchHandler.post(timeOutRunnable);
                else {
                    String[] RELATION = dbP.relationSelect(
                            "select uid_a, uid_b, relation from user_relation where uid_a = " + uid_l + " and uid_b = " + uid_h
                    );
                    if (RELATION[0] != null && RELATION[0].equals("-2"))
                        watchHandler.post(serverErrorRunnable);
                    else {
                        if (RELATION[0] == null) // impossible event
                            watchHandler.post(serverErrorRunnable);
                        else {
                            if (uid_l.equals(uid)) { // me in low
                                switch (RELATION[2]) {
                                    case "2": // 2 to -1
                                        int res = dbP.update("update user_relation set " +
                                                "relation = -1 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + watch_uid);
                                            watchHandler.post(unWatchRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "-1": // -1 to 2
                                        int res1 = dbP.update("update user_relation set " +
                                                "relation = 2 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res1 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + watch_uid);
                                            watchHandler.post(eachRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "1": // 1 to 0
                                        int res2 = dbP.update("update user_relation set " +
                                                "relation = 0 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res2 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + watch_uid);
                                            watchHandler.post(nothingRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "0": // 0 to 1
                                        int res3 = dbP.update("update user_relation set " +
                                                "relation = 1 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res3 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + watch_uid);
                                            watchHandler.post(watchRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                }
                            } else { // me in high
                                switch (RELATION[2]) {
                                    case "2": // 2 to 1
                                        int res = dbP.update("update user_relation set " +
                                                "relation = 1 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + watch_uid);
                                            watchHandler.post(unWatchRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "-1": // -1 to 0
                                        int res1 = dbP.update("update user_relation set " +
                                                "relation = 0 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res1 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num - 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num - 1) where uid = " + watch_uid);
                                            watchHandler.post(nothingRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "1": // 1 to 2
                                        int res2 = dbP.update("update user_relation set " +
                                                "relation = 2 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res2 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + watch_uid);
                                            watchHandler.post(eachRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                    case "0": // 0 to -1
                                        int res3 = dbP.update("update user_relation set " +
                                                "relation = -1 where uid_a = " + uid_l + " and uid_b = " + uid_h);
                                        if (res3 == 1) {
                                            dbP.update("update user_info_count set watch_num = (watch_num + 1) where uid = " + uid);
                                            dbP.update("update user_info_count set fans_num = (fans_num + 1) where uid = " + watch_uid);
                                            watchHandler.post(watchRunnable);
                                        } else
                                            watchHandler.post(serverErrorRunnable);
                                        break;
                                }
                            }
                        }
                    }
                }
                if (eachOtherImg != null)
                    eachOtherImg.setClickable(true);
                dbP.closeConn();
            }
        }).start();
    }

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        introList.clear();
        relationList.clear();
        watchUIDList.clear();
    }

    public void updateRelation(int position) {
        if (watchUIDList != null && watchUIDList.size() > position) {
            String UID_L = Integer.parseInt(UID) < Integer.parseInt(watchUIDList.get(position)) ? UID : watchUIDList.get(position);
            String UID_H = Integer.parseInt(UID) > Integer.parseInt(watchUIDList.get(position)) ? UID : watchUIDList.get(position);
            connectToUpdateRelation(UID, watchUIDList.get(position), UID_L, UID_H);
        }
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
            Toast.makeText(WatchActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WatchActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable nothingListRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(WatchActivity.this, "还没有关注任何人哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable watchRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WatchActivity.this, "成功关注Ta", Toast.LENGTH_SHORT).show();
            eachOtherImg.setImageResource(R.mipmap.ic_watch_blue_pink_36dp);
        }
    };

    private Runnable unWatchRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WatchActivity.this, "成功取消关注", Toast.LENGTH_SHORT).show();
            eachOtherImg.setImageResource(R.mipmap.ic_fans_pink_blue_36dp);
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WatchActivity.this, "你们俩不再有关系啦", Toast.LENGTH_SHORT).show();
            eachOtherImg.setImageResource(R.mipmap.ic_nothing_blue_36dp);
        }
    };

    private Runnable eachRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WatchActivity.this, "你们俩互相关注啦", Toast.LENGTH_SHORT).show();
            eachOtherImg.setImageResource(R.mipmap.ic_each_other_pink_36dp);
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
