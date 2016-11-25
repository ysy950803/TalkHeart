package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.WriteActivity;
import com.ysy.talkheart.adapters.ListOnItemClickListener;
import com.ysy.talkheart.adapters.HomeActiveListViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/22.
 */

public class HomeFragment extends StatedFragment {

    private FloatingActionButton addFab;
    private RecyclerView activeRecyclerView;

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nicknameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
    private List<Boolean> goodStatusList = new ArrayList<>();
    private List<Integer> goodNumList = new ArrayList<>();

    private HomeActiveListViewAdapter listViewAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        return view;
    }

    private void initData() {
        avatarList.add(R.drawable.me_avatar_boy);
        avatarList.add(R.drawable.me_avatar_girl);
        avatarList.add(R.drawable.me_avatar_boy);

        nicknameList.add("原子君");
        nicknameList.add("分子君");
        nicknameList.add("质子君");

        timeList.add("2分钟前");
        timeList.add("4分钟前");
        timeList.add("8分钟前");

        textList.add(getString(R.string.home_active_text));
        textList.add(getString(R.string.home_active_text_2));
        textList.add(getString(R.string.home_active_text_3));

        goodStatusList.add(false);
        goodStatusList.add(true);
        goodStatusList.add(false);

        goodNumList.add(8);
        goodNumList.add(96);
        goodNumList.add(192);
    }

    private void initView(View view) {
        addFab = (FloatingActionButton) view.findViewById(R.id.home_add_fab);
        activeRecyclerView = (RecyclerView) view.findViewById(R.id.home_active_listView);
        activeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listViewAdapter = new HomeActiveListViewAdapter(avatarList, nicknameList, timeList, textList, goodStatusList, goodNumList);
        activeRecyclerView.setAdapter(listViewAdapter);
    }

    private void clickListener() {
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), addFab, getString(R.string.trans_add));
                    startActivity(new Intent(getActivity(), WriteActivity.class), tAO.toBundle());
                } else
                    startActivity(new Intent(getActivity(), WriteActivity.class));
            }
        });
        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

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
