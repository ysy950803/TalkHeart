package com.ysy.talkheart.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.CommentListViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.RecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private String ACT_ID;
    private String UID;
    private String E_UID;
    private SwipeRefreshLayout refreshLayout;
    private Handler commentHandler;
    private FloatingActionButton commentFab;
    private CommentListViewAdapter listViewAdapter;
    private boolean isRefreshing = false;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<String> cmtidList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        setupActionBar();
        commentHandler = new Handler();
        initData();
        initView();
        refreshLayout.setRefreshing(true);
        refresh();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        E_UID = getIntent().getExtras().getString("e_uid");
        ACT_ID = getIntent().getExtras().getString("actid");
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.comment_refresh_layout);
        commentFab = (FloatingActionButton) findViewById(R.id.comment_fab);
        commentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(CommentActivity.this);
                if (!cd.isConnectingToInternet()) {
                    Toast.makeText(CommentActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                } else {
                    comment(E_UID, UID, ACT_ID);
                }
            }
        });

        RecyclerView commentRecyclerView = (RecyclerView) findViewById(R.id.comment_listView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {
                commentFab.hide();
            }

            @Override
            public void onScrollDown() {
                commentFab.show();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }
        };
        scrollListener.setScrollThreshold(4);
        commentRecyclerView.setOnScrollListener(scrollListener);

        listViewAdapter = new CommentListViewAdapter(this, avatarList, nicknameList, timeList, textList);
        commentRecyclerView.setAdapter(listViewAdapter);

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void comment(String e_uid, String uid, String actid) {
        Intent intent = new Intent(this, ReplyActivity.class);
        intent.putExtra("e_uid", e_uid);
        intent.putExtra("uid", uid);
        intent.putExtra("actid", actid);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(CommentActivity.this, commentFab, getString(R.string.trans_comment));
            startActivity(intent, tAO.toBundle());
        } else
            startActivity(intent);
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
        connectToGetComment(ACT_ID);
        return true;
    }

    private void connectToGetComment(final String actid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    commentHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.commentSelect(
                            "select sex, nickname, sendtime, content, cmtid, c.uid from user u, comment c where actid = " + actid + " and " +
                                    "c.uid = u.uid order by cmtid asc"
                    );
                    clearAllLists();
                    if (resList == null) {
                        commentHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        commentHandler.post(nothingRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            avatarList.add(resList.get(0).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(resList.get(1).get(i));
                            timeList.add(resList.get(2).get(i).substring(0, 19));
                            textList.add(resList.get(3).get(i));
                            cmtidList.add(resList.get(4).get(i));
                            uidList.add(resList.get(5).get(i));
                        }
                        commentHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    public void reply(int position) {
        if (E_UID.equals(uidList.get(position))) {
            Toast.makeText(this, "不能回复自己哦", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putExtra("uid", uidList.get(position));
            intent.putExtra("e_uid", E_UID);
            intent.putExtra("actid", ACT_ID);
            intent.putExtra("cmtid", cmtidList.get(position));
            intent.putExtra("nickname", nicknameList.get(position));
            startActivity(intent);
        }
    }

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        timeList.clear();
        textList.clear();
        cmtidList.clear();
        uidList.clear();
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

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(CommentActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(CommentActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(CommentActivity.this, "还没有任何评论哦，快抢沙发吧", Toast.LENGTH_SHORT).show();
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
}
