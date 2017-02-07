package com.ysy.talkheart.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzy.ninegrid.NineGridView;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.adapters.MeActiveListViewAdapter;
import com.ysy.talkheart.utils.SuperImageLoader;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDouleDialogClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActiveActivity extends DayNightActivity {

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<Integer> goodStatusList = new ArrayList<>();
    private List<String> goodNumList = new ArrayList<>();
    private List<String> actidList = new ArrayList<>();
    private List<String> imgInfoList = new ArrayList<>();
    private MeActiveListViewAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private Handler activeHandler;
    private String UID = "0";
    private String E_UID = "0";
    private String SEX = "1";
    private String NICKNAME = "加载中…";
    public ImageView goodImg;
    private boolean isSelf;
    private byte[] avatarBytes;
    private long timeNode;
//    private int fav_actid_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        setupActionBar(false);
        activeHandler = new Handler();
        initData();
        initView();
        clickListener();
        refreshLayout.setRefreshing(true);
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    private void initData() {
        avatarBytes = getIntent().getExtras().getByteArray("avatar");
        E_UID = getIntent().getExtras().getString("e_uid");
        UID = getIntent().getExtras().getString("uid");
        SEX = getIntent().getExtras().getString("sex");
        NICKNAME = getIntent().getExtras().getString("nickname");
        isSelf = E_UID.equals(UID);
    }

    private void initView() {
        NineGridView.setImageLoader(new SuperImageLoader());

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.me_active_refresh_layout);
        RecyclerView activeRecyclerView = (RecyclerView) findViewById(R.id.me_active_listView);

        activeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new MeActiveListViewAdapter(this, UID, avatarBytes, avatarList,
                nicknameList, timeList, textList, goodStatusList, goodNumList, imgInfoList);
        listViewAdapter.setFootLoadCallBack(new MeActiveListViewAdapter.FootLoadCallBack() {
            @Override
            public void onLoad() {
                refreshData(false);
            }
        });
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
                ConnectionDetector cd = new ConnectionDetector(ActiveActivity.this);
                if (cd.isConnectingToInternet()) {
                    if (isSelf) {
                        String items[] = {"收藏", "修改", "删除"};
                        showItemDialog(items, E_UID, actidList.get(position), textList.get(position));
                    } else {
                        String item[] = {"收藏"};
                        showItemDialog(item, E_UID, actidList.get(position), null);
                    }
                } else
                    Toast.makeText(ActiveActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refresh() {
        if (!isRefreshing) {
            isRefreshing = true;
            if (!refreshData(true)) {
                refreshLayout.setRefreshing(false);
                isRefreshing = false;
            }
        }
    }

    private boolean refreshData(boolean isHeadRefresh) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isHeadRefresh) {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String time = df.format(new Date());
            timeNode = Long.parseLong(time);
            listViewAdapter.setMaxExistCount(9);
            connectToGetActive(UID, isSelf ? UID : E_UID, Integer.parseInt(SEX), NICKNAME, 0);
        } else
            connectToGetActive(UID, isSelf ? UID : E_UID, Integer.parseInt(SEX), NICKNAME,
                    listViewAdapter.getItemCount() - 1);
        return true;
    }

    private void showItemDialog(String[] items, final String e_uid, final String actid, final String modify_content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new NoDouleDialogClickListener() {
            @Override
            protected void onNoDoubleClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        connectToMark(e_uid, actid);
                        break;
                    case 1:
                        openContentModify(e_uid, actid, modify_content);
                        break;
                    case 2:
                        connectToDelete(e_uid, actid);
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void updateGood(int position) {
        connectToUpdateGood(isSelf ? UID : E_UID, position);
    }

    private void connectToDelete(final String e_uid, final String actid) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("紧张的提示框").setMessage("确定要删除这条动态吗亲？（与之相关联信息都会删除哦）").setCancelable(true)
                .setPositiveButton("我意已决", new NoDouleDialogClickListener() {
                    @Override
                    protected void onNoDoubleClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DBProcessor dbP = new DBProcessor();
                                if (dbP.getConn(opts_o) == null) {
                                    activeHandler.post(timeOutRunnable);
                                } else {
                                    int res = dbP.delete(
                                            "delete from active where actid = " + actid
                                    );
                                    if (res == 1) {
                                        dbP.update("update user_info_count set act_num = (act_num - 1) where uid = " + e_uid);
                                        activeHandler.post(deleteRunnable);
                                    } else
                                        activeHandler.post(serverErrorRunnable);
                                }
                            }
                        }).start();
                    }
                }).setNegativeButton("再想想", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void connectToMark(final String e_uid, final String actid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    activeHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.insert(
                            "insert into mark(uid, actid) values(" + e_uid + ", " + actid + ")"
                    );
                    if (res == 1)
                        activeHandler.post(markRunnable);
                    else if (res == -1)
                        activeHandler.post(markErrorRunnable);
                    else
                        activeHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToGetActive(final String uid, final String e_uid, final int sex, final String nickname, final int loadPosition) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    activeHandler.post(timeOutRunnable);
                } else {
                    List<List<String>> resList = dbP.activeSelect(
                            "select a.actid, sendtime, goodnum, content, ifnull(isfav, -1) as isfav, img_info from " +
                                    "active a left join favorite f on f.actid = a.actid and f.uid = " + e_uid + " where " +
                                    "(sendtime + 0) < " + timeNode +
                                    " and a.uid = " + uid +
                                    " order by actid desc limit " + loadPosition + ", 10"
                    );
                    if (loadPosition == 0)
                        clearAllLists();
                    if (resList == null) {
                        activeHandler.post(serverErrorRunnable);
                    } else if (resList.get(0).size() == 0) {
                        if (loadPosition == 0)
                            activeHandler.post(headNothingRunnable);
                        else
                            activeHandler.post(footNothingRunnable);
                    } else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            avatarList.add(sex == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(nickname);
                            actidList.add(resList.get(0).get(i));
                            timeList.add(resList.get(1).get(i).substring(0, 19));
                            goodNumList.add(resList.get(2).get(i));
                            textList.add(resList.get(3).get(i));
                            goodStatusList.add(Integer.parseInt(resList.get(4).get(i)));
                            imgInfoList.add(resList.get(5).get(i));
                        }
                        resList.clear();
                        activeHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

//    private int getGoodStatus(int pos, List<String> actidList, List<List<String>> statusList) {
//        int isfav = -1;
//        if (statusList.get(0).size() == 0)
//            isfav = -1;
//        else {
//            if (fav_actid_index >= statusList.get(0).size())
//                isfav = -1;
//            else {
//                if (!actidList.get(pos).equals(statusList.get(0).get(fav_actid_index)))
//                    isfav = -1;
//                else if (actidList.get(pos).equals(statusList.get(0).get(fav_actid_index))) {
//                    isfav = Integer.parseInt(statusList.get(1).get(fav_actid_index)); // 0 or 1
//                    ++fav_actid_index;
//                }
//            }
//        }
//        return isfav;
//    }

    private void connectToUpdateGood(final String e_uid, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    activeHandler.post(timeOutRunnable);
                } else {
                    String actid = actidList.get(position);
                    if (goodStatusList.get(position) == 1) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "update favorite set isfav = 0 where uid = " + e_uid + " and actid = " + actid
                        );
                        if (res == 2)
                            goodStatusList.set(position, 0);
                        else
                            activeHandler.post(noGoodErrorRunnable);
                    } else if (goodStatusList.get(position) == -1) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "insert into favorite(uid, actid, isfav, favtime) values(" + e_uid + ", " + actid + ", 1, NOW())"
                        );
                        if (res == 2) {
                            if (!isSelf)
                                dbP.update("update user set isread = 0 where uid = " + UID);
                            goodStatusList.set(position, 1);
                        } else
                            activeHandler.post(goodErrorRunnable);
                    } else if (goodStatusList.get(position) == 0) {
                        int res = dbP.goodUpdate(
                                "update active set goodnum = " + goodNumList.get(position) + " where actid = " + actid,
                                "update favorite set isfav = 1 where uid = " + e_uid + " and actid = " + actid
                        );
                        if (res == 2) {
                            if (!isSelf)
                                dbP.update("update user set isread = 0 where uid = " + UID);
                            goodStatusList.set(position, 1);
                        } else
                            activeHandler.post(goodErrorRunnable);
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

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        goodStatusList.clear();
        timeList.clear();
        goodNumList.clear();
        textList.clear();
        actidList.clear();
        imgInfoList.clear();
//        fav_actid_index = 0;
    }

    private void openContentModify(String e_uid, String actid, String modify_content) {
        Intent intent = new Intent(this, ActiveModifyActivity.class);
        intent.putExtra("uid", e_uid);
        intent.putExtra("actid", actid);
        intent.putExtra("modify_content", modify_content);
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    public void openComment(int position) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("uid", UID);
        intent.putExtra("e_uid", E_UID);
        intent.putExtra("actid", actidList.get(position));
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    public void openPerson(int position) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra("uid", UID);
        intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
        intent.putExtra("nickname", nicknameList.get(position));
        intent.putExtra("e_uid", E_UID);
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    private Runnable headNothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(ActiveActivity.this, "还没有任何动态哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable footNothingRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setMaxExistCount(listViewAdapter.getItemCount() - 1);
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(ActiveActivity.this, "没有更多动态哦", Toast.LENGTH_SHORT).show();
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

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            refresh();
            Toast.makeText(ActiveActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable markRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "收藏成功啦", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable markErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveActivity.this, "已经收藏过了", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setIsLoading(false);
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(ActiveActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setIsLoading(false);
            listViewAdapter.notifyDataSetChanged();
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
}
