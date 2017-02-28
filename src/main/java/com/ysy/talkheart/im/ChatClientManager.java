package com.ysy.talkheart.im;

import android.text.TextUtils;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

public class ChatClientManager {

    private static ChatClientManager clientManager;

    private AVIMClient client;
    private String clientId;

    public synchronized static ChatClientManager getInstance() {
        if (null == clientManager)
            clientManager = new ChatClientManager();
        return clientManager;
    }

    private ChatClientManager() {

    }

    public void open(String clientId, AVIMClientCallback callback, AVIMConversationEventHandler eventHandler) {
        this.clientId = clientId;
        AVIMMessageManager.setConversationEventHandler(eventHandler);
        client = AVIMClient.getInstance(clientId);
        client.open(callback);
    }

    public AVIMClient getClient() {
        return client;
    }

    public String getClientId() {
        if (TextUtils.isEmpty(clientId)) {
//            throw new IllegalStateException("Please call ChatClientManager.open first.");
            return null;
        }
        return clientId;
    }
}
