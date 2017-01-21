package com.ysy.talkheart.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.adapters.MeMarkListViewAdapter;
import com.ysy.talkheart.utils.NoDouleDialogClickListener;

import java.util.ArrayList;
import java.util.List;

public class MarkActivity extends DayNightActivity {

    private MeMarkListViewAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<String> actidList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private String UID = "0";
    private Handler refreshHandler;
    private String[] opts_o;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        setupActionBar();
        refreshHandler = new Handler();
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
        UID = getIntent().getExtras().getString("uid");
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.me_mark_refresh_layout);
        RecyclerView markRecyclerView = (RecyclerView) findViewById(R.id.me_mark_listView);

        markRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeMarkListViewAdapter(this, uidList, avatarList, nicknameList, timeList, textList);
        markRecyclerView.setAdapter(listViewAdapter);

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

            }

            @Override
            public void onItemLongClick(View view, int position) {
                showItemDialog(actidList.get(position), position);
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
        connectToGetMark(UID);
        return true;
    }

    private void showItemDialog(final String actid, final int position) {
        final String items[] = {"删除"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new NoDouleDialogClickListener() {
            @Override
            protected void onNoDoubleClick(DialogInterface dialog, int which) {
                connectToDelete(actid, position);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void openPerson(int position) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra("uid", uidList.get(position));
        intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
        intent.putExtra("nickname", nicknameList.get(position));
        intent.putExtra("e_uid", UID);
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    private void connectToDelete(final String actid, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    refreshHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.delete(
                            "delete from mark where actid = " + actid + " and uid = " + UID
                    );
                    if (res == 1) {
                        timeList.remove(position);
                        textList.remove(position);
                        avatarList.remove(position);
                        nicknameList.remove(position);
                        actidList.remove(position);
                        refreshHandler.post(deleteRunnable);
                    } else if (res == -1)
                        refreshHandler.post(deleteErrorRunnable);
                    else
                        refreshHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToGetMark(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    refreshHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.markSelect(
                            "select sex, sendtime, nickname, content, m.actid, a.uid " +
                                    "from user u, mark m, active a where m.actid = a.actid and m.uid = " + uid + " and a.uid = u.uid " +
                                    "order by sendtime desc"
                    );
                    clearAllLists();
                    if (resList == null) {
                        refreshHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        refreshHandler.post(nothingRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            avatarList.add(resList.get(0).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            timeList.add(resList.get(1).get(i).substring(0, 19));
                            nicknameList.add(resList.get(2).get(i));
                            textList.add(resList.get(3).get(i));
                            actidList.add(resList.get(4).get(i));
                            uidList.add(resList.get(5).get(i));
                        }
                        refreshHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        timeList.clear();
        textList.clear();
        actidList.clear();
        uidList.clear();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(MarkActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable deleteErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MarkActivity.this, "删除失败啦，它舍不得离开呢", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(MarkActivity.this, "还没有任何收藏哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MarkActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MarkActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
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
