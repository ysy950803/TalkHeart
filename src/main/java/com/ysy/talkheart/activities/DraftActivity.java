package com.ysy.talkheart.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.MeDraftListViewAdapter;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleDialogClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class DraftActivity extends DayNightActivity {

    private MeDraftListViewAdapter listViewAdapter;
    @BindView(R.id.me_draft_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<String> dftidList = new ArrayList<>();
    private Handler draftHandler;
    private String UID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        setupActionBar(false);
        draftHandler = new Handler();
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
        UID = getIntent().getExtras().getString("uid");
    }

    private void initView() {
        RecyclerView draftRecyclerView = (RecyclerView) findViewById(R.id.me_draft_listView);
        draftRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeDraftListViewAdapter(timeList, textList);
        draftRecyclerView.setAdapter(listViewAdapter);

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
                Intent intent = new Intent(DraftActivity.this, WriteActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("dft_id", dftidList.get(position));
                intent.putExtra("dft_content", textList.get(position));
                intent.putExtra("opts_o", opts_o);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showItemDialog(dftidList.get(position), position);
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
        connectToGetDraft(UID);
        return true;
    }

    private void showItemDialog(final String dftid, final int position) {
        final String items[] = {"删除"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new NoDoubleDialogClickListener() {
            @Override
            protected void onNoDoubleClick(DialogInterface dialog, int which) {
                connectToDelete(dftid, position);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void connectToDelete(final String dftid, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    draftHandler.post(timeOutRunnable);
                else {
                    int res = dbP.delete(
                            "delete from draft where dftid = " + dftid
                    );
                    if (res == 1) {
                        timeList.remove(position);
                        textList.remove(position);
                        dftidList.remove(position);
                        draftHandler.post(deleteRunnable);
                    } else if (res == -1)
                        draftHandler.post(deleteErrorRunnable);
                    else
                        draftHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToGetDraft(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    draftHandler.post(timeOutRunnable);
                else {
                    List<List<String>> resList = dbP.draftSelect(
                            "select dftid, savetime, content from draft where uid = " + uid + " order by savetime desc"
                    );
                    clearAllLists();
                    if (resList == null)
                        draftHandler.post(serverErrorRunnable);
                    else if (resList.get(0).size() == 0)
                        draftHandler.post(nothingRunnable);
                    else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            dftidList.add(resList.get(0).get(i));
                            timeList.add(resList.get(1).get(i).substring(0, 19));
                            textList.add(resList.get(2).get(i));
                        }
                        draftHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void clearAllLists() {
        dftidList.clear();
        timeList.clear();
        textList.clear();
    }

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(DraftActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable deleteErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(DraftActivity.this, "删除失败啦，它舍不得离开呢", Toast.LENGTH_SHORT).show();
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

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(DraftActivity.this, "还没有任何草稿哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(DraftActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(DraftActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };
}
