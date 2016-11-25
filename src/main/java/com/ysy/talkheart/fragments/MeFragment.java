package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.ysy.talkheart.utils.DataCleanManager;
import com.ysy.talkheart.views.CircularImageView;

/**
 * Created by Shengyu Yao on 2016/11/22.
 */

public class MeFragment extends StatedFragment {

    private CircularImageView avatarImg;
    private TextView nicknameTv;
    private TextView activeNumTv;
    private TextView watchNumTv;
    private TextView fansNumTv;
    private TextView introductionTv;
    private LinearLayout markLayout;
    private LinearLayout draftLayout;
    private LinearLayout clearLayout;
    private LinearLayout exitLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
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
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public MeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initView(view);
        clickListener();
        return view;
    }

    private void initView(View view) {
        avatarImg = (CircularImageView) view.findViewById(R.id.me_avatar_img);
        nicknameTv = (TextView) view.findViewById(R.id.me_nickname_tv);
        activeNumTv = (TextView) view.findViewById(R.id.me_active_num_tv);
        watchNumTv = (TextView) view.findViewById(R.id.me_watch_num_tv);
        fansNumTv = (TextView) view.findViewById(R.id.me_fans_num_tv);
        introductionTv = (TextView) view.findViewById(R.id.me_introduction_tv);
        markLayout = (LinearLayout) view.findViewById(R.id.me_mark_layout);
        draftLayout = (LinearLayout) view.findViewById(R.id.me_draft_layout);
        clearLayout = (LinearLayout) view.findViewById(R.id.me_clear_layout);
        exitLayout = (LinearLayout) view.findViewById(R.id.me_exit_layout);
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

        activeNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), activeNumTv, getString(R.string.trans_active));
                    startActivity(new Intent(getActivity(), ActiveActivity.class), tAO.toBundle());
                } else {
                    startActivity(new Intent(getActivity(), ActiveActivity.class));
                }
            }
        });

        watchNumTv.setOnClickListener(new View.OnClickListener() {
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

        fansNumTv.setOnClickListener(new View.OnClickListener() {
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

        introductionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
