package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.activities.DraftActivity;
import com.ysy.talkheart.activities.FansActivity;
import com.ysy.talkheart.activities.HomeActivity;
import com.ysy.talkheart.activities.LoginActivity;
import com.ysy.talkheart.activities.MarkActivity;
import com.ysy.talkheart.activities.PersonActivity;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.DataCleanManager;
import com.ysy.talkheart.utils.DataProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.StringUtils;
import com.ysy.talkheart.utils.ViewTurnAnimation;
import com.ysy.talkheart.views.CircularImageView;

/**
 * Created by Shengyu Yao on 2016/11/22.
 */

public class MeFragment extends StatedFragment {

    private CircularImageView avatarImg;
    private TextView nicknameTv;
    private TextView activeNumTv;
    private LinearLayout activeNumLayout;
    private TextView watchNumTv;
    private LinearLayout watchNumLayout;
    private TextView fansNumTv;
    private LinearLayout fansNumLayout;
    private TextView introductionTv;
    private LinearLayout markLayout;
    private LinearLayout draftLayout;
    private LinearLayout clearLayout;
    private LinearLayout exitLayout;
    private LinearLayout introInputLayout;
    private EditText introEdt;
    private ImageView introDoneImg;
    private String NICKNAME = "加载中…";
    private String INTRODUCTION = "加载中…";
    private String SEX = "1";
    private String ACTIVE_NUM = "0";
    private String WATCH_NUM = "0";
    private String FANS_NUM = "0";
    private static final String FRAGMENT_TAG = "Me";
    private static final String OPTS_KEY = "opts_o";
    private String UID;
    private HomeActivity context;
    private Handler meFragmentHandler;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private boolean isSeen = false;
    private String[] opts_o;

    public MeFragment() {
        meFragmentHandler = new Handler();
    }

    public static MeFragment newInstance(String tag, String[] opts_o) {
        MeFragment fragment = new MeFragment();
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
            context = (HomeActivity) getActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initData();
        initView(view);
        clickListener();
        if (!isSeen) {
            refreshLayout.setRefreshing(true);
            refresh();
        }
        return view;
    }

    private void initData() {
        isSeen = context.getIsSeen();
        isRefreshing = false;
    }

    private void initView(View view) {
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.me_refresh_layout);
        avatarImg = (CircularImageView) view.findViewById(R.id.me_avatar_img);
        nicknameTv = (TextView) view.findViewById(R.id.me_nickname_tv);
        activeNumTv = (TextView) view.findViewById(R.id.me_active_num_tv);
        activeNumLayout = (LinearLayout) view.findViewById(R.id.me_active_layout);
        watchNumTv = (TextView) view.findViewById(R.id.me_watch_num_tv);
        watchNumLayout = (LinearLayout) view.findViewById(R.id.me_watch_layout);
        fansNumTv = (TextView) view.findViewById(R.id.me_fans_num_tv);
        fansNumLayout = (LinearLayout) view.findViewById(R.id.me_fans_layout);
        introductionTv = (TextView) view.findViewById(R.id.me_introduction_tv);
        markLayout = (LinearLayout) view.findViewById(R.id.me_mark_layout);
        draftLayout = (LinearLayout) view.findViewById(R.id.me_draft_layout);
        clearLayout = (LinearLayout) view.findViewById(R.id.me_clear_layout);
        exitLayout = (LinearLayout) view.findViewById(R.id.me_exit_layout);
        introInputLayout = (LinearLayout) view.findViewById(R.id.me_intro_input_layout);
        introEdt = (EditText) view.findViewById(R.id.me_intro_edt);
        introDoneImg = (ImageView) view.findViewById(R.id.me_intro_done_img);

        introInputLayout.setVisibility(View.GONE);
        introductionTv.setVisibility(View.VISIBLE);

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void clickListener() {
        avatarImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.putExtra("sex", SEX);
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), avatarImg, getString(R.string.trans_me_avatar));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        nicknameTv.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                startActivity(intent);
            }
        });

        activeNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), ActiveActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), activeNumTv, getString(R.string.trans_active));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        watchNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), WatchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), watchNumTv, getString(R.string.trans_watch));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        fansNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), FansActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), fansNumTv, getString(R.string.trans_fans));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        final ViewTurnAnimation animation = new ViewTurnAnimation(introductionTv, introInputLayout);
        introductionTv.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                introductionTv.startAnimation(animation.getSATo(0));
                introEdt.setText(introductionTv.getText().toString().equals("点击设置签名") ? "" : introductionTv.getText().toString());
                introEdt.requestFocus();
            }
        });

        introDoneImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                introInputLayout.startAnimation(animation.getSATo(0));
                String intro = introEdt.getText().toString();
                if (intro.length() > 24) {
                    Toast.makeText(context, "签名不能超过24个字符哦", Toast.LENGTH_SHORT).show();
                } else {
                    if (!intro.equals(introductionTv.getText().toString()) && !StringUtils.replaceBlank(introEdt.getText().toString()).equals(""))
                        connectToUpdateIntro(intro);
                }
            }
        });

        markLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), MarkActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), markLayout, getString(R.string.trans_mark));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        draftLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(getActivity(), DraftActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), draftLayout, getString(R.string.trans_draft));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        clearLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                clearCache();
            }
        });

        exitLayout.setOnTouchListener(new View.OnTouchListener() {
            long lastTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - lastTime > 5000) {
                            Toast.makeText(getActivity(), "强制更新已开启", Toast.LENGTH_SHORT).show();
                            context.forceCheckUpdate();
                        } else {
                            context.exitDialog();
                        }
                        break;
                }
                return true;
            }
        });
        setClickable(false);
    }

    public void getMeInfo() {
        refreshLayout.setRefreshing(true);
        refresh();
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
        connectToGetMeInfo(UID);
        return true;
    }

    private void connectToGetMeInfo(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    meFragmentHandler.post(timeOutRunnable);
                } else {
                    String[] res = dbP.meInfoSelect(
                            "select sex, nickname, intro, act_num, watch_num, fans_num from user u, user_info_count uic" +
                                    " where u.uid = " + uid + " and u.uid = uic.uid");
                    if (res[1] == null || res[1].equals("/(ㄒoㄒ)/~~")) {
                        meFragmentHandler.post(serverErrorRunnable);
                    } else {
                        SEX = res[0];
                        NICKNAME = res[1];
                        INTRODUCTION = res[2] == null ? "点击设置签名" : res[2];
                        ACTIVE_NUM = res[3];
                        WATCH_NUM = res[4];
                        FANS_NUM = res[5];
                        meFragmentHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToUpdateIntro(final String intro) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    meFragmentHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.update(
                            "update user set intro = '" + intro + "' where uid = " + UID
                    );
                    if (res == 1) {
                        meFragmentHandler.post(updateIntroRunnable);
                    } else {
                        meFragmentHandler.post(serverErrorRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void setClickable(boolean isAble) {
        nicknameTv.setClickable(isAble);
        activeNumLayout.setClickable(isAble);
        watchNumLayout.setClickable(isAble);
        fansNumLayout.setClickable(isAble);
        introductionTv.setClickable(isAble);
        markLayout.setClickable(isAble);
        draftLayout.setClickable(isAble);
        avatarImg.setClickable(isAble);
    }

    private void clearCache() {
        String cache_str = getString(R.string.me_draft_clear_cache);
        try {
            if (!cache_str.equals(DataCleanManager.getTotalCacheSize(getActivity()))) {
                cache_str = "成功清理" + DataCleanManager.getTotalCacheSize(getActivity()) + "缓存";
                DataCleanManager.clearAllCache(getContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), cache_str, Toast.LENGTH_SHORT).show();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            avatarImg.setImageResource(Integer.parseInt(SEX) == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
            nicknameTv.setText(new StringUtils().shortNickname(NICKNAME));
            introductionTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
            isSeen = true;
            context.setIsSeen(true);
        }
    };

    private Runnable updateIntroRunnable = new Runnable() {
        @Override
        public void run() {
            introductionTv.setText(introEdt.getText().toString());
            INTRODUCTION = introEdt.getText().toString();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            if (MeFragment.this.isAdded())
                Toast.makeText(getActivity(), "服务器君生病了，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (MeFragment.this.isAdded())
                Toast.makeText(getActivity(), "连接超时啦，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onSaveState(Bundle outState) {
        saveMeInfoState(outState);
        super.onSaveState(outState);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        readMeInfoState(savedInstanceState);
    }

    private void saveMeInfoState(Bundle outState) {
        if (isSeen) {
            outState.putString("me_sex", SEX);
            outState.putString("me_nickname", NICKNAME);
            outState.putString("me_act_num", ACTIVE_NUM);
            outState.putString("me_watch_num", WATCH_NUM);
            outState.putString("me_fans_num", FANS_NUM);
            outState.putString("me_intro", INTRODUCTION);
        }
    }

    private void readMeInfoState(Bundle savedInstanceState) {
        if (isSeen) {
            if (savedInstanceState.getString("me_nickname", null) != null) {
                SEX = savedInstanceState.getString("me_sex");
                NICKNAME = savedInstanceState.getString("me_nickname");
                ACTIVE_NUM = savedInstanceState.getString("me_act_num");
                WATCH_NUM = savedInstanceState.getString("me_watch_num");
                FANS_NUM = savedInstanceState.getString("me_fans_num");
                INTRODUCTION = savedInstanceState.getString("me_intro");
                avatarImg.setImageResource(Integer.parseInt(SEX) == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
                nicknameTv.setText(NICKNAME);
                introductionTv.setText(INTRODUCTION);
                activeNumTv.setText(ACTIVE_NUM);
                watchNumTv.setText(WATCH_NUM);
                fansNumTv.setText(FANS_NUM);
                setClickable(true);
            } else
                Toast.makeText(context, "内存君打瞌睡了，下拉刷新一下吧", Toast.LENGTH_SHORT).show();
        }
    }
}
