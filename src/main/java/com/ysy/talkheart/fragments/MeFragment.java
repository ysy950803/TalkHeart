package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.ysy.talkheart.utils.ActivitiesDestroyer;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.DataCleanManager;
import com.ysy.talkheart.utils.DataProcessor;
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

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String UID;

    private HomeActivity context;

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
                if (dbP.getConn() == null) {
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

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            avatarImg.setImageResource(Integer.parseInt(SEX) == 1 ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
            nicknameTv.setText(NICKNAME);
            introductionTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
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
            Toast.makeText(getActivity(), "服务器君生病了，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "连接超时啦，重试一下吧", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            UID = getArguments().getString(ARG_PARAM2);
            context = (HomeActivity) getActivity();
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
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), avatarImg, getString(R.string.trans_me_avatar));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        nicknameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                startActivity(intent);
            }
        });

        activeNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActiveActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), activeNumTv, getString(R.string.trans_active));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        watchNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WatchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), watchNumTv, getString(R.string.trans_watch));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        fansNumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FansActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), fansNumTv, getString(R.string.trans_fans));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
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
                Intent intent = new Intent(getActivity(), MarkActivity.class);
                intent.putExtra("uid", UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), markLayout, getString(R.string.trans_mark));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        draftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DraftActivity.class);
                intent.putExtra("uid", UID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), draftLayout, getString(R.string.trans_draft));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        clearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            exitDialog();
                        }
                        break;
                }
                return true;
            }
        });
        setClickable(false);
    }

    private void connectToUpdateIntro(final String intro) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
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

    private void exitDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle("可爱的提示框").setMessage("确定要退出登录切换用户吗亲？").setCancelable(false)
                .setPositiveButton("好哒", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataProcessor dp = new DataProcessor(getActivity());
                        dp.saveData("uid", "");
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }
                }).setNegativeButton("再想想", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
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
