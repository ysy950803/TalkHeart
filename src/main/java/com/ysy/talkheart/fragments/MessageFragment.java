package com.ysy.talkheart.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.adapters.MessageListViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/22.
 */

public class MessageFragment extends StatedFragment {

    private List<Integer> avatarList = new ArrayList<>();
    private List<String> nameActList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> quoteList = new ArrayList<>();
    private MessageListViewAdapter listViewAdapter;
    private RecyclerView msgRecyclerView;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
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

    public MessageFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initData();
        initView(view);
        clickListener();
        return view;
    }

    private void initData() {
        avatarList.add(R.drawable.me_avatar_boy);
        avatarList.add(R.drawable.me_avatar_girl);
        avatarList.add(R.drawable.me_avatar_boy);

        nameActList.add("原子君 与你连心");
        nameActList.add("分子君 与你连心");
        nameActList.add("质子君 与你连心");

        timeList.add("2分钟前");
        timeList.add("4分钟前");
        timeList.add("8分钟前");

        quoteList.add(getString(R.string.home_active_text));
        quoteList.add(getString(R.string.home_active_text_2));
        quoteList.add(getString(R.string.home_active_text_3));
    }

    private void initView(View view) {
        msgRecyclerView = (RecyclerView) view.findViewById(R.id.message_listView);
        msgRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listViewAdapter = new MessageListViewAdapter(avatarList, nameActList, timeList, quoteList);
        msgRecyclerView.setAdapter(listViewAdapter);
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

    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
    }
}
