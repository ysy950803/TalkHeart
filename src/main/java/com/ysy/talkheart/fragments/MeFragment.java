package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.activities.DraftActivity;
import com.ysy.talkheart.activities.FansActivity;
import com.ysy.talkheart.activities.LoginActivity;
import com.ysy.talkheart.activities.MarkActivity;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.DataCleanManager;
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
    private String SEX = "加载中…";
    private String ACTIVE_NUM = "0";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String UID;

    private Handler meFragmentHandler;

    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void connectToGetMeInfo(final String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                dbP.getConn();
                String[] res = dbP.meInfoSelect(
                        "select sex, nickname, intro from user where uid = " + uid,
                        "select count(actid) from active where uid = " + uid);
                if (res[1].equals("/(ㄒoㄒ)/~~")) {
                    meFragmentHandler.post(errorRunnable);
                } else {
                    SEX = res[0];
                    NICKNAME = res[1];
                    INTRODUCTION = res[2];
                    ACTIVE_NUM = res[3];
                    meFragmentHandler.post(successRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            avatarImg.setImageResource(Integer.parseInt(SEX) == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
            nicknameTv.setText(NICKNAME);
            introductionTv.setText(INTRODUCTION == null ? "点击设置签名" : INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            controlClick(true);
        }
    };

    private Runnable updateIntroRunnable = new Runnable() {
        @Override
        public void run() {
            introductionTv.setText(introEdt.getText().toString());
        }
    };

    private Runnable errorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "服务器君生病了，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            UID = getArguments().getString(ARG_PARAM2);
        }
    }

    public MeFragment() {
        meFragmentHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initView(view);
        clickListener();
        connectToGetMeInfo(UID);
        return view;
    }

    private void initView(View view) {
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
    }

    private void clickListener() {
        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nicknameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        activeNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), activeNumTv, getString(R.string.trans_active));
                    Intent intent = new Intent(getActivity(), ActiveActivity.class);
                    intent.putExtra("uid", UID);
                    intent.putExtra("sex", SEX);
                    intent.putExtra("nickname", NICKNAME);
                    startActivity(intent, tAO.toBundle());
                } else {
                    Intent intent = new Intent(getActivity(), ActiveActivity.class);
                    intent.putExtra("uid", UID);
                    intent.putExtra("sex", SEX);
                    intent.putExtra("nickname", NICKNAME);
                    startActivity(intent);
                }
            }
        });

        watchNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), watchNumTv, getString(R.string.trans_watch));
                    startActivity(new Intent(getActivity(), WatchActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(getActivity(), WatchActivity.class));
                }
            }
        });

        fansNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), fansNumTv, getString(R.string.trans_fans));
                    startActivity(new Intent(getActivity(), FansActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(getActivity(), FansActivity.class));
                }
            }
        });

        final ViewTurnAnimation animation = new ViewTurnAnimation(introductionTv, introInputLayout);
        introductionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                introductionTv.startAnimation(animation.getSATo(0));
                introEdt.setText(introductionTv.getText().toString().equals("点击设置签名") ? "" : introductionTv.getText().toString());
                introEdt.requestFocus();
            }
        });

        introDoneImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                introInputLayout.startAnimation(animation.getSATo(0));
                String intro = introEdt.getText().toString();
                if (!intro.equals(introductionTv.getText().toString()) && !intro.equals(""))
                    connectToUpdateIntro(intro);
            }
        });

        markLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), markLayout, getString(R.string.trans_mark));
                    startActivity(new Intent(getActivity(), MarkActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(getActivity(), MarkActivity.class));
                }
            }
        });

        draftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), draftLayout, getString(R.string.trans_draft));
                    startActivity(new Intent(getActivity(), DraftActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(getActivity(), DraftActivity.class));
                }
            }
        });

        clearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCache();
            }
        });

        exitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        controlClick(false);
    }

    private void connectToUpdateIntro(final String intro) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                dbP.getConn();
                int res = dbP.update(
                        "update user set intro = '" + intro + "' where uid = " + UID
                );
                if (res == 1) {
                    meFragmentHandler.post(updateIntroRunnable);
                } else {
                    meFragmentHandler.post(errorRunnable);
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void controlClick(boolean isAble) {
        nicknameTv.setClickable(isAble);
        activeNumLayout.setClickable(isAble);
        watchNumLayout.setClickable(isAble);
        fansNumLayout.setClickable(isAble);
        introductionTv.setClickable(isAble);
        markLayout.setClickable(isAble);
        draftLayout.setClickable(isAble);
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

    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
    }
}
