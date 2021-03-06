package com.ysy.talkheart.im.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.im.ChatClientManager;
import com.ysy.talkheart.im.ChatConstants;
import com.ysy.talkheart.im.fragments.SingleChatFragment;
import com.ysy.talkheart.utils.DBProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SingleChatActivity extends DayNightActivity {

    protected SingleChatFragment chatFragment;
    private String ME_UID;
    private String OBJ_UID;
    private String ME_NICKNAME;
    private String OBJ_NICKNAME;
    private byte[] ME_AVATAR;
    private byte[] OBJ_AVATAR;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
        initView(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        setupActionBar(false);
        initData(getIntent());
        initView(true);
    }

    private void initData(Intent intent) {
        opts_o = ((GlobalApp) getApplication()).getOpts_o();
        ME_UID = intent.getStringExtra("me_uid");
        OBJ_UID = intent.getStringExtra(ChatConstants.OBJ_ID);
        ME_NICKNAME = intent.getStringExtra("me_nickname");
        OBJ_NICKNAME = intent.getStringExtra("obj_nickname");
        ME_AVATAR = intent.getByteArrayExtra("me_avatar");
        OBJ_AVATAR = intent.getByteArrayExtra("obj_avatar");
    }

    private void initView(boolean isOnCreated) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(OBJ_NICKNAME);
        if (isOnCreated)
            chatFragment = (SingleChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_single_chat);
        getConversation(OBJ_UID);
    }

    public byte[] getME_AVATAR() {
        return ME_AVATAR;
    }

    public byte[] getOBJ_AVATAR() {
        return OBJ_AVATAR;
    }

    public String getME_NICKNAME() {
        return ME_NICKNAME;
    }

    public String getOBJ_NICKNAME() {
        return OBJ_NICKNAME;
    }

    private void getConversation(final String obj_uid) {
        final AVIMClient client = ChatClientManager.getInstance().getClient();
        AVIMConversationQuery query = client.getQuery();
        query.withMembers(Arrays.asList(obj_uid), true);
        query.whereEqualTo("customConversationType", 1);
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        chatFragment.setConversation(list.get(0));
                        updateConvNicknames(list.get(0));
                    } else {
                        HashMap<String, Object> attrs = new HashMap<>();
                        attrs.put("customConversationType", 1);
                        attrs.put(ME_UID, ME_NICKNAME);
                        attrs.put(OBJ_UID, OBJ_NICKNAME);
                        client.createConversation(Arrays.asList(obj_uid), null, attrs, false, true,
                                new AVIMConversationCreatedCallback() {
                                    @Override
                                    public void done(AVIMConversation conv, AVIMException e) {
                                        chatFragment.setConversation(conv);
                                    }
                                });
                    }
                }
            }
        });
    }

    private void updateConvNicknames(final AVIMConversation conv) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) != null) {
                    String[] nicknames = dbP.nicknamesSelect(
                            "select u1.nickname, u2.nickname from user u1, user u2 " +
                                    "where u1.uid = " + ME_UID + " and u2.uid = " + OBJ_UID
                    );
                    if (nicknames != null)
                        if (!ME_NICKNAME.equals(nicknames[0]) || !OBJ_NICKNAME.equals(nicknames[1])) {
                            conv.setAttribute(ME_UID, nicknames[0]);
                            conv.setAttribute(OBJ_UID, nicknames[1]);
                            conv.updateInfoInBackground(new AVIMConversationCallback() {
                                @Override
                                public void done(AVIMException e) {
                                    ((GlobalApp) getApplication()).setNicknamesUpdated(true);
                                }
                            });
                            ME_NICKNAME = nicknames[0];
                            OBJ_NICKNAME = nicknames[1];
                        }
                }
            }
        }).start();
    }
}
