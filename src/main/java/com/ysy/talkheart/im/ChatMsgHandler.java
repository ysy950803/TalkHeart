package com.ysy.talkheart.im;

import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.im.events.ChatTypeMsgEvent;
import com.ysy.talkheart.utils.DBProcessor;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

public class ChatMsgHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {

    private Context context;
    private String[] opts_o;
    private String clientID;
    private String AVATAR_UPLOAD_URL = "";
    private byte[] ME_AVATAR;
    private byte[] OBJ_AVATAR;
    private String ME_NICKNAME;
    private String OBJ_NICKNAME;

    public ChatMsgHandler(Context context) {
        this.context = context;
        AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
    }

    @Override
    public void onMessage(AVIMTypedMessage msg, AVIMConversation conv, AVIMClient client) {
        try {
            clientID = ChatClientManager.getInstance().getClientId();
            if (client.getClientId().equals(clientID)) {
                if (!msg.getFrom().equals(clientID)) {
                    sendEvent(msg, conv);
                    if (!((GlobalApp) context).getHomeVisible() &&
                            NotificationUtils.isShowNotification(conv.getConversationId()))
                        sendNotification(msg, conv);
                }
            } else
                client.close(null);
        } catch (IllegalStateException e) {
            client.close(null);
        }
    }

    private void sendEvent(AVIMTypedMessage msg, AVIMConversation conv) {
        ChatTypeMsgEvent event = new ChatTypeMsgEvent();
        event.msg = msg;
        event.conv = conv;
        EventBus.getDefault().post(event);
    }

    private void sendNotification(final AVIMTypedMessage msg, final AVIMConversation conversation) {
        final String content = msg instanceof AVIMTextMessage ? ((AVIMTextMessage) msg).getText() : "[新消息]";
        opts_o = ((GlobalApp) context.getApplicationContext()).getOpts_o();

        final AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(16 * 1000);
        httpClient.get(AVATAR_UPLOAD_URL + "/" + msg.getFrom() + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        OBJ_AVATAR = responseBody;
                        httpClient.get(AVATAR_UPLOAD_URL + "/" + clientID + "_avatar_img_thumb.jpg",
                                new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        ME_AVATAR = responseBody;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                DBProcessor dbP = new DBProcessor();
                                                if (dbP.getConn(opts_o) != null) {
                                                    String[] nicknames = dbP.nicknamesSelect(
                                                            "select u1.nickname, u2.nickname from user u1, user u2 " +
                                                                    "where u1.uid = " + clientID + " and u2.uid = " + msg.getFrom());
                                                    if (nicknames != null) {
                                                        ME_NICKNAME = nicknames[0];
                                                        OBJ_NICKNAME = nicknames[1];
                                                        Intent intent = getNTFCIntent(true, msg, conversation);
                                                        NotificationUtils.showNotification(context, OBJ_NICKNAME, content, intent);
                                                    } else {
                                                        Intent intent = getNTFCIntent(false, msg, conversation);
                                                        NotificationUtils.showNotification(context, "有朋友找你哦", "[新消息]", intent);
                                                    }
                                                } else {
                                                    Intent intent = getNTFCIntent(false, msg, conversation);
                                                    NotificationUtils.showNotification(context, "有朋友找你哦", "[新消息]", intent);
                                                }
                                                dbP.closeConn();
                                            }
                                        }).start();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Intent intent = getNTFCIntent(false, msg, conversation);
                                        NotificationUtils.showNotification(context, "有朋友找你哦", "[新消息]", intent);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Intent intent = getNTFCIntent(false, msg, conversation);
                        NotificationUtils.showNotification(context, "有朋友找你哦", "[新消息]", intent);
                    }
                });
    }

    private Intent getNTFCIntent(boolean isFull, final AVIMTypedMessage msg, final AVIMConversation conv) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.putExtra("me_uid", clientID);
        if (isFull) {
            intent.putExtra(ChatConstants.CONV_ID, conv.getConversationId());
            intent.putExtra(ChatConstants.OBJ_ID, msg.getFrom());
            intent.putExtra("me_avatar", ME_AVATAR);
            intent.putExtra("obj_avatar", OBJ_AVATAR);
            intent.putExtra("me_nickname", ME_NICKNAME);
            intent.putExtra("obj_nickname", OBJ_NICKNAME);
        }
        return intent;
    }
}
