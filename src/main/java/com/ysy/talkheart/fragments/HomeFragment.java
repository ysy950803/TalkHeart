package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.activities.HomeActivity;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.activities.WriteActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.adapters.HomeActiveListViewAdapter;
import com.ysy.talkheart.utils.RecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/22.
 */

public class HomeFragment extends StatedFragment {

    private SwipeRefreshLayout refreshLayout;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<Integer> goodStatusList = new ArrayList<>();
    private List<String> goodNumList = new ArrayList<>();
    private List<String> actidList = new ArrayList<>();

    private HomeActiveListViewAdapter listViewAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String UID;

    private boolean isRefreshing = false;
    private Handler homeActiveHandler;

    private HomeActivity context;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            UID = getArguments().getString(ARG_PARAM2);
        }
        context = (HomeActivity) getActivity();
        homeActiveHandler = new Handler();
    }

    public HomeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initData();
        initView(view);
        clickListener();
        refreshLayout.setRefreshing(true);
        refresh();
        return view;
    }

    private void initData() {

    }

    private void initView(View view) {
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.home_active_refresh_layout);
        final FloatingActionButton addFab = context.getAddFab();
        final BottomNavigationBar navigationBar = context.getBottomNavigationBar();

        RecyclerView activeRecyclerView = (RecyclerView) view.findViewById(R.id.home_active_listView);
        activeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewScrollListener scrollListener =  new RecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {
                addFab.hide();
                navigationBar.hide();
            }

            @Override
            public void onScrollDown() {
                addFab.show();
                navigationBar.show();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }
        };
        scrollListener.setScrollThreshold(4);
        activeRecyclerView.setOnScrollListener(scrollListener);

        listViewAdapter = new HomeActiveListViewAdapter(this, avatarList, nicknameList, timeList, textList, goodStatusList, goodNumList);
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
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                showItemDialog(UID, actidList.get(position));
            }
        });
    }

    private void showItemDialog(final String uid, final String actid) {
        final String items[] = {"收藏"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectToMark(uid, actid);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void connectToMark(final String uid, final String actid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    homeActiveHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.insert(
                            "insert into mark(uid, actid) values(" + uid + ", " + actid + ")"
                    );
                    if (res == 1)
                        homeActiveHandler.post(markRunnable);
                    else if (res == -1)
                        homeActiveHandler.post(markErrorRunnable);
                    else
                        homeActiveHandler.post(serverErrorRunnable);
                }
            }
        }).start();
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
        ConnectionDetector cd = new ConnectionDetector(getActivity());
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToGetActive(UID);
        return true;
    }

    private void connectToGetActive(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    homeActiveHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.homeActiveSelect(
                            "select actid, sex, nickname, sendtime, content, goodnum from active a, user u where a.uid = u.uid and u.uid in (" +
                                    "select uid_a from user_relation where uid_b = " + uid + " and relation in (-1, 2) " +
                                    "union select uid_b from user_relation where uid_a = " + uid + " and relation in (1, 2) " +
                                    "union select uid from user where uid = " + uid + ")" +
                                    " order by actid desc"
                    );
                    List<List<String>> statusList = dbP.goodSelect(
                            "select actid, isfav from favorite where uid = " + uid +
                                    " order by actid desc"
                    );
                    clearAllLists();
                    if (statusList == null || resList == null) {
                        homeActiveHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        homeActiveHandler.post(nothingListRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            actidList.add(resList.get(0).get(i));
                            avatarList.add(resList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(resList.get(2).get(i));
                            timeList.add(resList.get(3).get(i).substring(0, 19));
                            textList.add(resList.get(4).get(i));
                            goodNumList.add(resList.get(5).get(i));
                            goodStatusList.add(getGoodStatus(i, actidList, statusList));
                        }
                        homeActiveHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

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

    public ImageView goodImg;

    public void updateGood(int position) {
        connectToUpdateGood(UID, position);
    }

    private void connectToUpdateGood(final String uid, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    homeActiveHandler.post(timeOutRunnable);
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
                            homeActiveHandler.post(noGoodErrorRunnable);
                    } else if (goodStatusList.get(position) == -1) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "insert into favorite(uid, actid, isfav) values(" + uid + ", " + actid + ", 1)"
                        );
                        if (res == 2)
                            goodStatusList.set(position, 1);
                        else
                            homeActiveHandler.post(goodErrorRunnable);
                    } else if (goodStatusList.get(position) == 0) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "update favorite set isfav = 1 where uid = " + uid + " and actid = " + actid
                        );
                        if (res == 2)
                            goodStatusList.set(position, 1);
                        else
                            homeActiveHandler.post(goodErrorRunnable);
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

    private Runnable markRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "收藏成功啦", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable markErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "已经收藏过了", Toast.LENGTH_SHORT).show();
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

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable nothingListRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "还没有关注任何动态哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable goodErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "连心失败了，请刷新重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable noGoodErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "心连心不易断，请刷新重试", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
    }
}
