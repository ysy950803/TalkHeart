package com.ysy.talkheart.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
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

            }
        });

        watchNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fansNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

            }
        });

        draftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        clearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        exitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
