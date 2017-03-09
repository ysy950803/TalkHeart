package com.ysy.talkheart.im.adapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.im.ChatConstants;
import com.ysy.talkheart.im.activities.HomeActivity;
import com.ysy.talkheart.im.activities.SingleChatActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class ChatListViewAdapter extends SuperRecyclerViewAdapter {

    private HomeActivity context;
    private List<AVIMConversation> convList;
    private String AVATAR_UPLOAD_URL = "";
    private String UID;
    private ProgressDialog waitDialog;
    private byte[] OBJ_AVATAR;

    public ChatListViewAdapter(HomeActivity context, List<AVIMConversation> convList,
                               String UID) {
        this.context = context;
        this.convList = convList;
        this.AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
        this.UID = UID;
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView nickNameTv;
        CircularImageView avatarImg;
        TextView timeTv;
        TextView msgTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            nickNameTv = (TextView) itemView.findViewById(R.id.chat_list_nickname_tv);
            msgTv = (TextView) itemView.findViewById(R.id.chat_list_msg_tv);
            timeTv = (TextView) itemView.findViewById(R.id.chat_list_time_tv);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.chat_list_avatar_img);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false));
    }

    @Override
    public int getItemCount() {
        return convList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        final String obj_uid = getOBJ_UID(convList.get(position).getMembers());
        downloadAvatar(context, AVATAR_UPLOAD_URL + "/" + obj_uid + "_avatar_img_thumb.jpg"
                , holder.avatarImg, R.drawable.me_avatar_null);
        updateItemTextInfo(convList.get(position), holder.timeTv, holder.msgTv, holder.nickNameTv, obj_uid);

        final int pos = Integer.parseInt(position + "");
        holder.itemView.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (cd.isConnectingToInternet()) {
                    if (obj_uid == null || obj_uid.equals(""))
                        Toast.makeText(context, "请不要急，数据正在准备", Toast.LENGTH_SHORT).show();
                    else
                        openChat((String) convList.get(pos).getAttribute(UID), holder.nickNameTv.getText().toString(), obj_uid);
                } else
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            }
        });
        super.onBindViewHolder(viewHolder, position);
    }

    private void openChat(final String me_nickname, final String obj_nickname, final String obj_uid) {
        final AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(16 * 1000);
        httpClient.get(AVATAR_UPLOAD_URL + "/" + obj_uid + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        waitDialog = ProgressDialog.show(context, "请稍后", "正在连接服务……");
                        super.onStart();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        OBJ_AVATAR = responseBody;
                        getSecondAvatar(httpClient, me_nickname, obj_nickname, obj_uid);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        OBJ_AVATAR = null;
                        getSecondAvatar(httpClient, me_nickname, obj_nickname, obj_uid);
                    }
                });
    }

    private void getSecondAvatar(AsyncHttpClient httpClient, final String me_nickname,
                                 final String obj_nickname, final String obj_uid) {
        waitDialog.dismiss();
        httpClient.get(AVATAR_UPLOAD_URL + "/" + UID + "_avatar_img_thumb.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        waitDialog = ProgressDialog.show(context, "请稍后", "正在连接通讯服务……");
                        super.onStart();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        gotoSingleChat(responseBody, me_nickname, obj_nickname, obj_uid);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        gotoSingleChat(null, me_nickname, obj_nickname, obj_uid);
                    }
                });
    }

    private void gotoSingleChat(byte[] responseBody, String me_nickname, String obj_nickname, String obj_uid) {
        waitDialog.dismiss();
        Intent intent = new Intent(context, SingleChatActivity.class);
        intent.putExtra("me_uid", UID);
        intent.putExtra("me_nickname", me_nickname);
        intent.putExtra("me_avatar", responseBody);
        intent.putExtra(ChatConstants.OBJ_ID, obj_uid);
        intent.putExtra("obj_nickname", obj_nickname);
        intent.putExtra("obj_avatar", OBJ_AVATAR);
        context.startActivity(intent);
    }

    private String getOBJ_UID(List<String> members) {
        if (members.get(0).equals(UID))
            return members.get(1);
        else
            return members.get(0);
    }

    private void updateItemTextInfo(final AVIMConversation conv, final TextView timeTv, final TextView msgTv,
                                    TextView nicknameTv, String obj_uid) {
        nicknameTv.setText((String) conv.getAttribute(obj_uid));
        conv.queryMessages(new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
                if (e == null && list.size() > 0) {
                    AVIMMessage msg = list.get(list.size() - 1);
                    if (null != msg) {
                        Date date = new Date(msg.getTimestamp());
                        SimpleDateFormat sDFt = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                        timeTv.setText(sDFt.format(date));
                        msgTv.setText(getMsgShorthand(msg));
                    } else {
                        timeTv.setText("");
                        msgTv.setText("");
                    }
                } else {
                    SimpleDateFormat sDFt = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                    timeTv.setText(sDFt.format(conv.getCreatedAt()));
                    msgTv.setText("[还没有消息]");
                }
            }
        });
    }

    private static CharSequence getMsgShorthand(AVIMMessage msg) {
        if (msg instanceof AVIMTypedMessage) {
            AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(
                    ((AVIMTypedMessage) msg).getMessageType());
            switch (type) {
                case TextMessageType:
                    return ((AVIMTextMessage) msg).getText();
                case ImageMessageType:
                    return "[图片]";
                case LocationMessageType:
                    return "[位置]";
                case AudioMessageType:
                    return "[语音]";
                default:
                    CharSequence shortHand = "";
                    if (msg instanceof LCChatMessageInterface) {
                        LCChatMessageInterface msgInterface = (LCChatMessageInterface) msg;
                        shortHand = msgInterface.getShorthand();
                    }
                    if (TextUtils.isEmpty(shortHand)) {
                        shortHand = "[未知]";
                    }
                    return shortHand;
            }
        } else
            return "[暂不支持此消息类型]";
    }

    private interface LCChatMessageInterface {
        String getShorthand();
    }
}
