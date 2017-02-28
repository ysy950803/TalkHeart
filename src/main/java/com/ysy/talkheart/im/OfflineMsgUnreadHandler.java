package com.ysy.talkheart.im;

import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.ysy.talkheart.im.activities.HomeActivity;

import java.util.List;

public class OfflineMsgUnreadHandler extends AVIMConversationEventHandler {

    private HomeActivity context;

    public OfflineMsgUnreadHandler(HomeActivity context) {
        this.context = context;
    }

    @Override
    public void onMemberLeft(AVIMClient avimClient, AVIMConversation avimConversation, List<String> list, String s) {

    }

    @Override
    public void onMemberJoined(AVIMClient avimClient, AVIMConversation avimConversation, List<String> list, String s) {

    }

    @Override
    public void onKicked(AVIMClient avimClient, AVIMConversation avimConversation, String s) {

    }

    @Override
    public void onInvited(AVIMClient avimClient, AVIMConversation avimConversation, String s) {

    }

    @Override
    public void onOfflineMessagesUnread(AVIMClient client, AVIMConversation conv, int unreadCount) {
        if (unreadCount > 0) {
            Toast.makeText(context, "你有 " + unreadCount + " 条未读会话", Toast.LENGTH_SHORT).show();
            context.getChatUnreadImg().setVisibility(View.VISIBLE);
        }
        super.onOfflineMessagesUnread(client, conv, unreadCount);
    }
}
