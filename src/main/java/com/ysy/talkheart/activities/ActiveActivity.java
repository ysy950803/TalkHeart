package com.ysy.talkheart.activities;

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
import com.ysy.talkheart.adapters.MeActiveListViewAdapter;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;

import java.util.ArrayList;
import java.util.List;

public class ActiveActivity extends AppCompatActivity {

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<Integer> goodStatusList = new ArrayList<>();
    private List<String> goodNumList = new ArrayList<>();

    private List<String> actidList = new ArrayList<>();

    private MeActiveListViewAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;

    private Handler refreshHandler;

    private String UID = "加载中…";
    private String SEX = "加载中…";
    private String NICKNAME = "加载中…";

    public ImageView goodImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        setupActionBar();

        initData();
        initView();
        clickListener();
        refreshHandler = new Handler();

        refreshLayout.setRefreshing(true);
        refresh();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        SEX = getIntent().getExtras().getString("sex");
        NICKNAME = getIntent().getExtras().getString("nickname");
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.me_active_refresh_layout);
        RecyclerView activeRecyclerView = (RecyclerView) findViewById(R.id.me_active_listView);

        activeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeActiveListViewAdapter(this, avatarList, nicknameList, timeList, textList, goodStatusList, goodNumList);
        activeRecyclerView.setAdapter(listViewAdapter);

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
            public void onItemClick(View view, final int position) {

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
        connectToGetActive(UID, Integer.parseInt(SEX), NICKNAME);
        return true;
    }

    private void connectToGetActive(final String uid, final int sex, final String nickname) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    refreshHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.meActiveSelect(
                            "select actid, sendtime, goodnum, content from active where uid = " + uid +
                                    " order by actid desc"
                    );
                    List<List<String>> statusList = dbP.goodSelect(
                            "select actid, isfav from favorite where uid = " + uid +
                                    " order by actid desc"
                    );
                    clearAllLists();
                    if (statusList == null || resList == null) {
                        refreshHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        refreshHandler.post(nothingRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            avatarList.add(sex == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(nickname);

                            actidList.add(resList.get(0).get(i));
                            goodStatusList.add(getGoodStatus(i, actidList, statusList));

                            timeList.add(resList.get(1).get(i).substring(0, 19));
                            goodNumList.add(resList.get(2).get(i));
                            textList.add(resList.get(3).get(i));
                        }
                        refreshHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private int fav_actid_index = 0;

    private int getGoodStatus(int pos, List<String> actidList, List<List<String>> statusList) {
        int isfav = -1;
        if (statusList.get(0).size() == 0)
            isfav = -1;
        else {
            if (fav_actid_index >= statusList.get(0).size())
                isfav = -1;
            else {
                if (!actidList.get(pos).equals(statusList.get(0).get(fav_actid_index)))
                    isfav = -1;
                else if (actidList.get(pos).equals(statusList.get(0).get(fav_actid_index))) {
                    isfav = Integer.parseInt(statusList.get(1).get(fav_actid_index)); // 0 or 1
                    ++fav_actid_index;
                }
            }
        }
        return isfav;
    }

    public void updateGood(int position) {
        connectToUpdateGood(UID, position);
    }

    private void connectToUpdateGood(final String uid, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    refreshHandler.post(timeOutRunnable);
                } else {
                    String actid = actidList.get(position);
                    if (goodStatusList.get(position) == 1) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "update favorite set isfav = 0 where uid = " + uid + " and actid = " + actid
                        );
                        if (res == 2)
                            goodStatusList.set(position, 0);
                        else
                            refreshHandler.post(noGoodErrorRunnable);
                    } else if (goodStatusList.get(position) == -1) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "insert into favorite(uid, actid, isfav) values(" + uid + ", " + actid + ", 1)"
                        );
                        if (res == 2)
                            goodStatusList.set(position, 1);
                        else
                            refreshHandler.post(goodErrorRunnable);
                    } else if (goodStatusList.get(position) == 0) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "update favorite set isfav = 1 where uid = " + uid + " and actid = " + actid
                        );
                        if (res == 2)
                            goodStatusList.set(position, 1);
                        else
                            refreshHandler.post(goodErrorRunnable);
                    }
                }
                if (goodImg != null) {
                    goodImg.setClickable(true);
                    goodImg = null;
                }
                dbP.closeConn();
            }
        }).start();
    }

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(ActiveActivity.this, "还没有任何动态哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable goodErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "连心失败了，请刷新重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable noGoodErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "心连心不易断，请刷新重试", Toast.LENGTH_SHORT).show();
        }
    };

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        goodStatusList.clear();
        timeList.clear();
        goodNumList.clear();
        textList.clear();
        actidList.clear();
        fav_actid_index = 0;
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
