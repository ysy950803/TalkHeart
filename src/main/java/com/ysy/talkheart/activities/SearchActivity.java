package com.ysy.talkheart.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.SearchUserListViewAdapter;
import com.ysy.talkheart.bases.DayNightNoActionBarActivity;
import com.ysy.talkheart.im.ChatConstants;
import com.ysy.talkheart.im.activities.SingleChatActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.KeyboardChangeListener;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends DayNightNoActionBarActivity {

    @BindView(R.id.search_edt)
    EditText searchEdt;
    @BindView(R.id.search_img)
    ImageView searchImg;
    @BindView(R.id.search_user_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private SearchUserListViewAdapter listViewAdapter;
    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> introList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private String UID;
    private Handler searchHandler;
    private boolean isKeyboardShow = false;
    private boolean isFromChat = false;

    private ProgressDialog waitDialog;
    private String AVATAR_UPLOAD_URL = "";
    private String meNickname;
    private byte[] objAvatar;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.search_toolbar));
        setupActionBar();
        initData();
        initView();
        clickListener();
        searchHandler = new Handler();

        getWatch();
        new KeyboardChangeListener(this).setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener() {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight) {
                isKeyboardShow = isShow;
            }
        });
    }

    private void initData() {
        isFromChat = getIntent().getExtras().getBoolean("from_chat", false);
        if (isFromChat) {
            meNickname = getIntent().getStringExtra("me_nickname");
            Toast.makeText(this, "点击任意朋友即可对话", Toast.LENGTH_SHORT).show();
        }
        UID = getIntent().getExtras().getString("uid");
        AVATAR_UPLOAD_URL = getResources().getString(R.string.url_avatar_upload);
    }

    private void initView() {
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        RecyclerView searchRecyclerView = (RecyclerView) findViewById(R.id.search_recyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewAdapter = new SearchUserListViewAdapter(this, uidList, avatarList, nicknameList, introList);
        searchRecyclerView.setAdapter(listViewAdapter);
    }

    private void clickListener() {
        searchImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                searchUser(searchEdt.getText().toString());
            }
        });

        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ConnectionDetector cd = new ConnectionDetector(SearchActivity.this);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(SearchActivity.this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else {
                    if (isFromChat) {
                        if (uidList.get(position).equals(UID))
                            Toast.makeText(SearchActivity.this, "不能和自己聊天哦", Toast.LENGTH_SHORT).show();
                        else
                            openChat(position);
                    } else
                        openPerson(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void getWatch() {
        refreshLayout.setRefreshing(true);
        refresh();
    }

    private void refresh() {
        if (!isRefreshing) {
            isRefreshing = true;
            if (!searchUser(searchEdt.getText().toString())) {
                refreshLayout.setRefreshing(false);
                isRefreshing = false;
            }
        }
    }

    private void openPerson(int position) {
        Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
        intent.putExtra("uid", uidList.get(position));
        intent.putExtra("sex", avatarList.get(position) == R.drawable.me_avatar_boy ? "1" : "0");
        intent.putExtra("nickname", nicknameList.get(position));
        intent.putExtra("e_uid", UID);
        intent.putExtra("opts_o", opts_o);
        startActivity(intent);
    }

    private void openChat(final int position) {
        final AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(16 * 1000);
        httpClient.get(AVATAR_UPLOAD_URL + "/" + uidList.get(position) + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        waitDialog = ProgressDialog.show(SearchActivity.this, "请稍后", "正在连接服务……");
                        super.onStart();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        objAvatar = responseBody;
                        getSecondAvatar(httpClient, position);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        objAvatar = null;
                        getSecondAvatar(httpClient, position);
                    }
                });
    }

    private void getSecondAvatar(AsyncHttpClient httpClient, final int position) {
        waitDialog.dismiss();
        httpClient.get(AVATAR_UPLOAD_URL + "/" + UID + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        waitDialog = ProgressDialog.show(SearchActivity.this, "请稍后", "正在连接服务……");
                        super.onStart();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        gotoSingleChat(responseBody, position);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        gotoSingleChat(null, position);
                    }
                });
    }

    private void gotoSingleChat(byte[] responseBody, int position) {
        waitDialog.dismiss();
        Intent intent = new Intent(SearchActivity.this, SingleChatActivity.class);
        intent.putExtra("me_uid", UID);
        intent.putExtra("me_nickname", meNickname);
        intent.putExtra("me_avatar", responseBody);
        intent.putExtra(ChatConstants.OBJ_ID, uidList.get(position));
        intent.putExtra("obj_nickname", nicknameList.get(position));
        intent.putExtra("obj_avatar", objAvatar);
        startActivity(intent);
        finish();
    }

    private boolean searchUser(String nicknameLike) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (StringUtils.replaceBlank(nicknameLike).equals("")) {
                connectToSearchUser(null);
            } else
                connectToSearchUser(nicknameLike);
        }
        return true;
    }

    private void connectToSearchUser(final String nicknameLike) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    searchHandler.post(timeOutRunnable);
                else {
                    String sql;
                    if (nicknameLike == null)
                        sql = "select nickname, sex, intro, uid from user, user_relation " +
                                "where (uid_a = uid and uid_b = " + UID + " and relation in (-1, 2)) or " +
                                "(uid_a = " + UID + " and uid_b = uid and relation in (1, 2))";
                    else
                        sql = "select nickname, sex, intro, uid from user where nickname like '%" + nicknameLike + "%'";
                    List<List<String>> resList = dbP.searchUserSelect(sql);
                    clearAllLists();
                    if (resList == null)
                        searchHandler.post(serverErrorRunnable);
                    else if (resList.get(0).size() == 0)
                        searchHandler.post(nothingRunnable);
                    else if (resList.get(0).size() > 0) {
                        for (int i = 0; i < resList.get(0).size(); i++) {
                            nicknameList.add(resList.get(0).get(i));
                            avatarList.add(resList.get(1).get(i).equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                            introList.add(resList.get(2).get(i) == null ? "未设置签名" : resList.get(2).get(i));
                            uidList.add(resList.get(3).get(i));
                        }
                        searchHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void clearAllLists() {
        avatarList.clear();
        nicknameList.clear();
        introList.clear();
        uidList.clear();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshing = false;
            refreshLayout.setRefreshing(false);
            listViewAdapter.notifyDataSetChanged();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshing = false;
            refreshLayout.setRefreshing(false);
            Toast.makeText(SearchActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshing = false;
            refreshLayout.setRefreshing(false);
            Toast.makeText(SearchActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable nothingRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshing = false;
            refreshLayout.setRefreshing(false);
            listViewAdapter.notifyDataSetChanged();
            Toast.makeText(SearchActivity.this, "没有找到那个Ta", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isKeyboardShow) {
                InputMethodManager iMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null)
                    iMM.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else
                onBackPressed();
            return true;
        }
        return onOptionsItemSelected(item);
    }
}
