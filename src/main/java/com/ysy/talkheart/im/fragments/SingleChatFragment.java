package com.ysy.talkheart.im.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.im.AVInputBottomBar;
import com.ysy.talkheart.im.NotificationUtils;
import com.ysy.talkheart.im.activities.SingleChatActivity;
import com.ysy.talkheart.im.adapters.MultipleItemAdapter;
import com.ysy.talkheart.im.events.ChatTypeMsgEvent;
import com.ysy.talkheart.im.events.ChatTypeMsgResentEvent;
import com.ysy.talkheart.im.events.InputBottomBarTextEvent;
import com.ysy.talkheart.utils.ConnectionDetector;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class SingleChatFragment extends Fragment {

    private AVIMConversation chatConv;
    private MultipleItemAdapter itemAdapter;
    @BindView(R.id.single_chat_recyclerView)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    @BindView(R.id.single_chat_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.single_chat_input_bar)
    AVInputBottomBar inputBottomBar;

    private SingleChatActivity context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (SingleChatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_chat, container, false);
        initView(view);
        EventBus.getDefault().register(this);
        return view;
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        itemAdapter = new MultipleItemAdapter(context);
        recyclerView.setAdapter(itemAdapter);

        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (cd.isConnectingToInternet()) {
                    AVIMMessage msg = itemAdapter.getFirstMsg();
                    if (null != chatConv && msg != null) {
                        chatConv.queryMessages(msg.getMessageId(), msg.getTimestamp(), 20, new AVIMMessagesQueryCallback() {
                            @Override
                            public void done(List<AVIMMessage> list, AVIMException e) {
                                refreshLayout.setRefreshing(false);
                                if (e == null) {
                                    if (null != list && list.size() > 0) {
                                        itemAdapter.addMessageList(list);
                                        itemAdapter.notifyDataSetChanged();
                                        layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                                    }
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, "有点小错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                } else
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != chatConv)
            NotificationUtils.addTag(chatConv.getConversationId());
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationUtils.removeTag(chatConv.getConversationId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void setConversation(AVIMConversation conv) {
        if (null != conv) {
            chatConv = conv;
            refreshLayout.setEnabled(true);
            inputBottomBar.setTag(chatConv.getConversationId());
            fetchMsg();
            NotificationUtils.addTag(conv.getConversationId());
        }
    }

    private void fetchMsg() {
        if (null != chatConv) {
            chatConv.queryMessages(new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> list, AVIMException e) {
                    if (e == null) {
                        itemAdapter.setMsgList(list);
                        recyclerView.setAdapter(itemAdapter);
                        itemAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    }
                }
            });
        }
    }

    public void onEvent(InputBottomBarTextEvent event) {
        if (null != chatConv && null != event) {
            if (!TextUtils.isEmpty(event.sendContent) && chatConv.getConversationId().equals(event.tag)) {
                AVIMTextMessage msg = new AVIMTextMessage();
                msg.setText(event.sendContent);
                itemAdapter.addMessage(msg);
                itemAdapter.notifyDataSetChanged();
                scrollToBottom();
                chatConv.sendMessage(msg, new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                        itemAdapter.notifyDataSetChanged();
                        ((GlobalApp) getActivity().getApplication()).setMeMsgSent(true);
                    }
                });
            }
        }
    }

    public void onEvent(ChatTypeMsgEvent event) {
        if (null != chatConv && null != event &&
                chatConv.getConversationId().equals(event.conv.getConversationId())) {
            itemAdapter.addMessage(event.msg);
            itemAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    public void onEvent(ChatTypeMsgResentEvent event) {
        if (null != chatConv && null != event) {
            if (AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == event.msg.getMessageStatus()
                    && chatConv.getConversationId().equals(event.msg.getConversationId())) {
                chatConv.sendMessage(event.msg, new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                        itemAdapter.notifyDataSetChanged();
                    }
                });
                itemAdapter.notifyDataSetChanged();
            }
        }
    }

    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(itemAdapter.getItemCount() - 1, 0);
    }
}
