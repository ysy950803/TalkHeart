package com.ysy.talkheart.im.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.ysy.talkheart.im.ChatClientManager;
import com.ysy.talkheart.im.activities.SingleChatActivity;
import com.ysy.talkheart.im.viewholders.ChatViewHolder;
import com.ysy.talkheart.im.viewholders.LeftTextHolder;
import com.ysy.talkheart.im.viewholders.RightTextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipleItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ITEM_LEFT_TEXT = 0;
    private final int ITEM_RIGHT_TEXT = 1;

    private List<AVIMMessage> msgList = new ArrayList<>();
    private SingleChatActivity context;

    public MultipleItemAdapter(SingleChatActivity context) {
        this.context = context;
    }

    public void setMsgList(List<AVIMMessage> msg) {
        msgList.clear();
        if (null != msg)
            msgList.addAll(msg);
    }

    public void addMessageList(List<AVIMMessage> msg) {
        msgList.addAll(0, msg);
    }

    public void addMessage(AVIMMessage msg) {
        msgList.addAll(Arrays.asList(msg));
    }

    public AVIMMessage getFirstMsg() {
        if (null != msgList && msgList.size() > 0)
            return msgList.get(0);
        else
            return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LEFT_TEXT)
            return new LeftTextHolder(parent.getContext(), parent);
        else if (viewType == ITEM_RIGHT_TEXT)
            return new RightTextHolder(parent.getContext(), parent);
        else
            return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ChatViewHolder) holder).bindData(msgList.get(position));
        if (holder instanceof LeftTextHolder) {
            ((LeftTextHolder) holder).showTimeView(shouldShowTime(position));
            ((LeftTextHolder) holder).setNicknameAndAvatar(context.getOBJ_NICKNAME(), context.getOBJ_AVATAR());
        } else if (holder instanceof RightTextHolder) {
            ((RightTextHolder) holder).showTimeView(shouldShowTime(position));
            ((RightTextHolder) holder).setNicknameAndAvatar(context.getME_NICKNAME(), context.getME_AVATAR());
        }
    }

    @Override
    public int getItemViewType(int position) {
        AVIMMessage msg = msgList.get(position);
        if (msg.getFrom().equals(ChatClientManager.getInstance().getClientId()))
            return ITEM_RIGHT_TEXT;
        else
            return ITEM_LEFT_TEXT;
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    private boolean shouldShowTime(int position) {
        if (position == 0)
            return true;
        long lastTime = msgList.get(position - 1).getTimestamp();
        long curTime = msgList.get(position).getTimestamp();
        long TIME_INTERVAL = 10 * 60 * 1000;
        return curTime - lastTime > TIME_INTERVAL;
    }
}
