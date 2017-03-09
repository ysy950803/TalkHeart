package com.ysy.talkheart.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.CommentActivity;
import com.ysy.talkheart.im.activities.HomeActivity;
import com.ysy.talkheart.activities.PersonActivity;
import com.ysy.talkheart.activities.ReplyActivity;
import com.ysy.talkheart.adapters.MessageListViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.RecyclerViewScrollListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageFragment extends StatedFragment {

    @BindView(R.id.message_listView)
    RecyclerView msgRecyclerView;
    @BindView(R.id.msg_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nameActList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> contentList = new ArrayList<>();
    private List<String> quoteList = new ArrayList<>();
    private List<String> actidList = new ArrayList<>();
    private List<String> cmtidList = new ArrayList<>();
    private List<String> uidPList = new ArrayList<>();
    private MessageListViewAdapter listViewAdapter;
    private boolean isRefreshing = false;
    private Handler msgHandler;
    private static final String FRAGMENT_TAG = "Msg";
    private static final String OPTS_KEY = "opts_o";
    private String UID;
    private HomeActivity context;
    private String[] opts_o;
    private long timeNode;

    public static MessageFragment newInstance(String tag, String[] opts_o) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_TAG, tag);
        args.putStringArray(OPTS_KEY, opts_o);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            opts_o = getArguments().getStringArray(OPTS_KEY);
            UID = getArguments().getString(FRAGMENT_TAG);
        }
        context = (HomeActivity) getActivity();
        msgHandler = new Handler();
    }

    public MessageFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initData();
        initView(view);
        clickListener();
        refreshLayout.setRefreshing(true);
        refresh();
        return view;
    }

    private void initData() {
        isRefreshing = false;
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);

        final BottomNavigationBar navigationBar = context.getBottomNavigationBar();
        msgRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {
                navigationBar.hide(false);
            }

            @Override
            public void onScrollDown() {
                navigationBar.show();
            }
        };
        scrollListener.setScrollThreshold(4);
        msgRecyclerView.setOnScrollListener(scrollListener);

        listViewAdapter = new MessageListViewAdapter(this, uidPList, avatarList, nameActList, timeList, contentList, quoteList);
        listViewAdapter.setFootLoadCallBack(new MessageListViewAdapter.FootLoadCallBack() {
            @Override
            public void onLoad() {
                refreshData(false);
            }
        });
        msgRecyclerView.setAdapter(listViewAdapter);

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
        ConnectionDetector cd = new ConnectionDetector(getActivity());
        if (!cd.isConnectingToInternet() && MessageFragment.this.isAdded()) {
            Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isHeadRefresh) {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String time = df.format(new Date());
            timeNode = Long.parseLong(time);
            listViewAdapter.setMaxExistCount(9);
            connectToGetMsg(UID, 0);
        } else
            connectToGetMsg(UID, listViewAdapter.getItemCount() - 1);
        return true;
    }

    private void connectToGetMsg(final String e_uid, final int loadPosition) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    msgHandler.post(timeOutRunnable);
                else {
                    List<List<String>> cmtList = dbP.msgCmtSelect(
                            "(select flag_cmt, sex, nickname, c.sendtime, c.content, a.content, a.actid, u.uid, cmtid from comment_flag, user u, active a, comment c " +
                                    "where (c.sendtime + 0) < " + timeNode +
                                    " and a.uid = " + e_uid + " and c.uid_p = " + e_uid + " and u.uid != " + e_uid + " and c.actid = a.actid and c.uid = u.uid and c.cmtid_p is null) " +
                                    "union " +
                                    "(select flag_reply, sex, nickname, c1.sendtime, c1.content, c2.content, c1.actid, c1.uid, c1.cmtid from comment_flag, user u, comment c1, comment c2 " +
                                    "where (c1.sendtime + 0) < " + timeNode +
                                    " and u.uid = c1.uid and c2.uid = " + e_uid + " and c1.cmtid_p = c2.cmtid) " +
                                    "union " +
                                    "(select flag_replyfav, sex, nickname, c.sendtime, c.content, a.content, c.actid, u.uid, cmtid from comment_flag, user u, comment c, active a " +
                                    "where (c.sendtime + 0) < " + timeNode +
                                    " and c.uid = u.uid and c.uid != " + e_uid + " and c.uid_p = " + e_uid + " and cmtid_p = -1 and a.actid = c.actid) " +
                                    "union " +
                                    "(select flag_fav, sex, nickname, favtime, flag_content, content, a.actid, u.uid, flag_cmtid from comment_flag, user u, favorite f, active a " +
                                    "where (favtime + 0) < " + timeNode +
                                    " and u.uid != " + e_uid + " and u.uid = f.uid and f.actid = a.actid and f.isfav = 1 and a.uid = " + e_uid + ") order by sendtime desc limit " + loadPosition + ", 10"
                    );
                    if (loadPosition == 0)
                        clearAllLists();
                    if (cmtList == null)
                        msgHandler.post(serverErrorRunnable);
                    else if (cmtList.get(0).size() == 0) {
                        if (loadPosition == 0)
                            msgHandler.post(headNothingListRunnable);
                        else
                            msgHandler.post(footNothingListRunnable);
                    } else if (cmtList.get(0).size() > 0) {
                        for (int i = 0; i < cmtList.get(0).size(); i++) {
                            avatarList.add(cmtList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            nicknameList.add(cmtList.get(2).get(i));
                            timeList.add(cmtList.get(3).get(i).substring(0, 19));
                            actidList.add(cmtList.get(6).get(i));
                            uidPList.add(cmtList.get(7).get(i));
                            String flag = cmtList.get(0).get(i);
                            switch (flag) {
                                case "1": // comment
                                    nameActList.add(cmtList.get(2).get(i) + " 评论了你");
                                    contentList.add(cmtList.get(4).get(i));
                                    quoteList.add(cmtList.get(5).get(i));
                                    cmtidList.add(cmtList.get(8).get(i));
                                    break;
                                case "2": // reply
                                    nameActList.add(cmtList.get(2).get(i) + " 回复了你");
                                    contentList.add(cmtList.get(4).get(i));
                                    quoteList.add(cmtList.get(5).get(i));
                                    cmtidList.add(cmtList.get(8).get(i));
                                    break;
                                case "3": // fav
                                    nameActList.add(cmtList.get(2).get(i) + " 与你连心");
                                    contentList.add("");
                                    quoteList.add(cmtList.get(5).get(i));
                                    cmtidList.add("");
                                    break;
                                case "4": // replyfav
                                    nameActList.add(cmtList.get(2).get(i) + " 感谢了你");
                                    contentList.add(cmtList.get(4).get(i));
                                    quoteList.add(cmtList.get(5).get(i));
                                    cmtidList.add(cmtList.get(8).get(i));
                                    break;
                            }
                        }
                        cmtList.clear();
                        if (loadPosition == 0 && context.getIsRead() == 0)
                            dbP.update("update user set isread = 1 where uid = " + e_uid);
                        msgHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    public void getNewMsg() {
        refreshLayout.setRefreshing(true);
        refresh();
    }

    public void reply(int position) {
        Intent intent = new Intent(context, ReplyActivity.class);
        intent.putExtra("uid", uidPList.get(position));
        intent.putExtra("e_uid", UID);
        intent.putExtra("actid", actidList.get(position));
        intent.putExtra("cmtid", cmtidList.get(position));
        intent.putExtra("opts_o", opts_o);
        intent.putExtra("nickname_p", nicknameList.get(position));
        startActivity(intent);
    }

    public void openComment(int position) {
        Intent intent = new Intent(context, CommentActivity.class);
        intent.putExtra("uid", uidPList.get(position));
        intent.putExtra("e_uid", UID);
        intent.putExtra("actid", actidList.get(position));
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    public void openPerson(int position) {
        Intent intent = new Intent(context, PersonActivity.class);
        intent.putExtra("uid", uidPList.get(position));
        intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
        intent.putExtra("nickname", nicknameList.get(position));
        intent.putExtra("e_uid", UID);
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    private void clearAllLists() {
        avatarList.clear();
        nameActList.clear();
        timeList.clear();
        contentList.clear();
        cmtidList.clear();
        actidList.clear();
        quoteList.clear();
        uidPList.clear();
        nicknameList.clear();
    }

//    private void sortListsByTime() {
//        quickSort(timeList, 0, timeList.size() - 1);
//    }
//
//    private int getMid(List<String> timeList, int low, int high) {
//        String time_temp = timeList.get(low);
//        int avatar_temp = avatarList.get(low);
//        String nameAct_temp = nameActList.get(low);
//        String content_temp = contentList.get(low);
//        String cmtid_temp = cmtidList.get(low);
//        String actid_temp = actidList.get(low);
//        String quote_temp = quoteList.get(low);
//        String uidp_temp = uidPList.get(low);
//        String nickname_temp = nicknameList.get(low);
//
//        while (low < high) {
//            while (low < high && timeList.get(high).compareTo(time_temp) < 0)
//                high--;
//            timeList.set(low, timeList.get(high));
//            avatarList.set(low, avatarList.get(high));
//            nameActList.set(low, nameActList.get(high));
//            contentList.set(low, contentList.get(high));
//            cmtidList.set(low, cmtidList.get(high));
//            actidList.set(low, actidList.get(high));
//            quoteList.set(low, quoteList.get(high));
//            uidPList.set(low, uidPList.get(high));
//            nicknameList.set(low, nicknameList.get(high));
//
//            while (low < high && timeList.get(low).compareTo(time_temp) > 0)
//                low++;
//            timeList.set(high, timeList.get(low));
//            avatarList.set(high, avatarList.get(low));
//            nameActList.set(high, nameActList.get(low));
//            contentList.set(high, contentList.get(low));
//            cmtidList.set(high, cmtidList.get(low));
//            actidList.set(high, actidList.get(low));
//            quoteList.set(high, quoteList.get(low));
//            uidPList.set(high, uidPList.get(low));
//            nicknameList.set(high, nicknameList.get(low));
//        }
//        timeList.set(low, time_temp);
//        avatarList.set(low, avatar_temp);
//        nameActList.set(low, nameAct_temp);
//        contentList.set(low, content_temp);
//        cmtidList.set(low, cmtid_temp);
//        actidList.set(low, actid_temp);
//        quoteList.set(low, quote_temp);
//        uidPList.set(low, uidp_temp);
//        nicknameList.set(low, nickname_temp);
//        return low;
//    }
//
//    private void quickSort(List<String> timeList, int low, int high) {
//        if (low < high) {
//            int mid = getMid(timeList, low, high);
//            quickSort(timeList, low, mid - 1);
//            quickSort(timeList, mid + 1, high);
//        }
//    }

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setIsLoading(false);
            listViewAdapter.notifyDataSetChanged();
            if (MessageFragment.this.isAdded())
                Toast.makeText(context, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setIsLoading(false);
            listViewAdapter.notifyDataSetChanged();
            if (MessageFragment.this.isAdded())
                Toast.makeText(context, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable headNothingListRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            if (MessageFragment.this.isAdded())
                Toast.makeText(context, "还没有消息哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
            context.getMsgUnreadImg().setVisibility(View.INVISIBLE);
        }
    };

    private Runnable footNothingListRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.setMaxExistCount(listViewAdapter.getItemCount() - 1);
            listViewAdapter.notifyDataSetChanged();
            if (MessageFragment.this.isAdded())
                Toast.makeText(context, "没有更多消息哦", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
            context.getMsgUnreadImg().setVisibility(View.INVISIBLE);
        }
    };

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            listViewAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
            context.getMsgUnreadImg().setVisibility(View.INVISIBLE);
            context.setIsRead(1);
        }
    };

    public void refreshFragmentUI() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        Resources res = getResources();
        int childCount = msgRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            theme.resolveAttribute(R.attr.colorPlaintViewBG, typedValue, true);
            CardView childView = (CardView) msgRecyclerView.getChildAt(i);
            childView.setCardBackgroundColor(res.getColor(typedValue.resourceId));

            theme.resolveAttribute(R.attr.colorName, typedValue, true);
            TextView tv = (TextView) childView.findViewById(R.id.msg_name_act_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));
            tv = (TextView) childView.findViewById(R.id.msg_content_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));

            theme.resolveAttribute(R.attr.colorPlainText, typedValue, true);
            tv = (TextView) childView.findViewById(R.id.msg_time_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));

            theme.resolveAttribute(R.attr.colorQuoteBG, typedValue, true);
            LinearLayout quoteLayout = (LinearLayout) childView.findViewById(R.id.msg_quote_layout);
            quoteLayout.setBackgroundResource(typedValue.resourceId);

            theme.resolveAttribute(R.attr.colorQuoteText, typedValue, true);
            tv = (TextView) childView.findViewById(R.id.msg_quote_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));
        }

        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler"); // can not change this arg
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("a");
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(msgRecyclerView));
            RecyclerView.RecycledViewPool recycledViewPool = msgRecyclerView.getRecycledViewPool();
            recycledViewPool.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RecyclerView getMsgRecyclerView() {
        return msgRecyclerView;
    }
}
