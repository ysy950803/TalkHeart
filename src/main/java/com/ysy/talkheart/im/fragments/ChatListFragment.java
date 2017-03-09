package com.ysy.talkheart.im.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.im.activities.HomeActivity;
import com.ysy.talkheart.activities.SearchActivity;
import com.ysy.talkheart.im.adapters.ChatListViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.RecyclerViewScrollListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListFragment extends StatedFragment {

    private static final String FRAGMENT_TAG = "ChatList";
    private String UID;

    @BindView(R.id.chat_list_add_fab)
    FloatingActionButton addFab;
    @BindView(R.id.chat_list_recyclerView)
    RecyclerView chatListRecyclerView;
    @BindView(R.id.chat_list_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private HomeActivity context;
    private String[] opts_o;
    private static final String OPTS_KEY = "opts_o";

    private ProgressDialog waitDialog;
    private boolean isRefreshing = false;
    private ChatListViewAdapter listViewAdapter;

    private List<AVIMConversation> convList = new ArrayList<>();
    private Handler chatListHandler;

    public RecyclerView getChatListRecyclerView() {
        return chatListRecyclerView;
    }

    public static ChatListFragment newInstance(String tag, String[] opts_o) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_TAG, tag);
        args.putStringArray(OPTS_KEY, opts_o);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            opts_o = getArguments().getStringArray(OPTS_KEY);
            UID = getArguments().getString(FRAGMENT_TAG);
        }
        context = (HomeActivity) getActivity();
        chatListHandler = new Handler();
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        final BottomNavigationBar navigationBar = context.getBottomNavigationBar();

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {
                navigationBar.hide(false);
                addFab.hide();
            }

            @Override
            public void onScrollDown() {
                navigationBar.show();
                addFab.show();
            }
        };
        scrollListener.setScrollThreshold(4);
        chatListRecyclerView.setOnScrollListener(scrollListener);

        listViewAdapter = new ChatListViewAdapter(context, convList, UID);
        chatListRecyclerView.setAdapter(listViewAdapter);

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        initData();
        initView(view);
        clickListener();
        refreshLayout.setRefreshing(true);
        refresh();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (context.getChatUnreadImg() != null && context.getChatUnreadImg().getVisibility() == View.VISIBLE)
            refreshData();
        super.onHiddenChanged(hidden);
    }

    private void initData() {
        isRefreshing = false;
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

    public boolean refreshData() {
        ConnectionDetector cd = new ConnectionDetector(context);
        if (cd.isConnectingToInternet()) {
            AVIMClient client = AVIMClient.getInstance(UID);
            if (client != null) {
                client.close(new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient client1, AVIMException e) {
                        if (e == null)
                            client1.open(new AVIMClientCallback() {
                                @Override
                                public void done(AVIMClient client2, AVIMException e) {
                                    if (e == null)
                                        queryData(client2);
                                    else
                                        endRefresh(true);
                                }
                            });
                        else
                            endRefresh(true);
                    }
                });
            } else
                endRefresh(true);
        } else {
            Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void queryData(AVIMClient client) {
        AVIMConversationQuery query = client.getQuery();
        query.limit(20);
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(final List<AVIMConversation> list, AVIMException e) {
                if (e == null) {
                    clearAllLists();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (AVIMConversation conv : list)
                                convList.add(conv);
                            chatListHandler.post(listLoadedRunnable);
                        }
                    }).start();
                } else
                    endRefresh(true);
            }
        });
    }

    private Runnable listLoadedRunnable = new Runnable() {
        @Override
        public void run() {
            if (convList.size() == 0)
                Toast.makeText(context, "还没有任何会话哦", Toast.LENGTH_SHORT).show();
            endRefresh(false);
        }
    };

    private void endRefresh(boolean isError) {
        if (!isError) {
            listViewAdapter.notifyDataSetChanged();
            context.getChatUnreadImg().setVisibility(View.INVISIBLE);
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        } else {
            AVIMClient.getInstance(UID).open(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client, AVIMException e) {
                    Toast.makeText(context, "网络不太好，请稍后刷新重试", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                    isRefreshing = false;
                }
            });
        }
    }

    private void clickListener() {
        addFab.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (cd.isConnectingToInternet())
                    chatConnect(UID);
                else
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chatConnect(final String uid) {
        waitDialog = ProgressDialog.show(context, "请稍后", "正在为你搜寻好友……");
        AVIMClient client = AVIMClient.getInstance(uid);
        if (client != null) {
            client.open(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client1, AVIMException e) {
                    if (e == null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DBProcessor dbP = new DBProcessor();
                                if (dbP.getConn(opts_o) != null) {
                                    String nickname = dbP.pwSelect("select nickname from user where uid = " + uid);
                                    if (nickname != null) {
                                        Intent intent = new Intent(context, SearchActivity.class);
                                        intent.putExtra("uid", uid);
                                        intent.putExtra("me_nickname", nickname);
                                        intent.putExtra("opts_o", opts_o);
                                        intent.putExtra("from_chat", true);
                                        startActivity(intent);
                                    } else
                                        Toast.makeText(context, "连接服务失败，请重试", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(context, "连接服务超时，请重试", Toast.LENGTH_SHORT).show();
                                waitDialog.dismiss();
                                dbP.closeConn();
                            }
                        }).start();
                    } else {
                        waitDialog.dismiss();
                        Toast.makeText(context, "通讯服务连接失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            waitDialog.dismiss();
            Toast.makeText(context, "通讯服务连接失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshFragmentUI() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        Resources res = getResources();

        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        addFab.setBackgroundTintList(ColorStateList.valueOf(res.getColor(typedValue.resourceId)));

        int childCount = chatListRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            theme.resolveAttribute(R.attr.colorPlaintViewBG, typedValue, true);
            CardView childView = (CardView) chatListRecyclerView.getChildAt(i);
            childView.setCardBackgroundColor(res.getColor(typedValue.resourceId));

            theme.resolveAttribute(R.attr.colorName, typedValue, true);
            TextView tv = (TextView) childView.findViewById(R.id.chat_list_nickname_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));

            theme.resolveAttribute(R.attr.colorPlainText, typedValue, true);
            tv = (TextView) childView.findViewById(R.id.chat_list_time_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));
            tv = (TextView) childView.findViewById(R.id.chat_list_msg_tv);
            tv.setTextColor(res.getColor(typedValue.resourceId));
        }

        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("a"); // "clear"
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(chatListRecyclerView));
            RecyclerView.RecycledViewPool recycledViewPool = chatListRecyclerView.getRecycledViewPool();
            recycledViewPool.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAllLists() {
        convList.clear();
    }
}
