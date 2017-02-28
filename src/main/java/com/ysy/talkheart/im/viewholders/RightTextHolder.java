package com.ysy.talkheart.im.viewholders;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.ysy.talkheart.R;
import com.ysy.talkheart.im.events.ChatTypeMsgResentEvent;
import com.ysy.talkheart.views.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RightTextHolder extends ChatViewHolder {

    @BindView(R.id.single_chat_right_time)
    TextView timeView;

    @BindView(R.id.single_chat_right_text)
    TextView contentView;

    @BindView(R.id.single_chat_right_name)
    TextView nicknameView;

    @BindView(R.id.single_chat_right_status_layout)
    FrameLayout statusView;

    @BindView(R.id.single_chat_right_progressbar)
    ProgressBar loadingBar;

    @BindView(R.id.single_chat_right_error)
    ImageView errorView;

    @BindView(R.id.single_chat_right_avatar)
    CircularImageView avatarImg;

    private AVIMMessage msg;

    public RightTextHolder(Context context, ViewGroup root) {
        super(context, root, R.layout.item_single_chat_right);
    }

    @OnClick(R.id.single_chat_right_error)
    void onErrorClick(View v) {
        ChatTypeMsgResentEvent resentEvent = new ChatTypeMsgResentEvent();
        resentEvent.msg = msg;
        EventBus.getDefault().post(resentEvent);
    }

    @Override
    public void bindData(Object o) {
        msg = (AVIMMessage) o;
        SimpleDateFormat sDFt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String time = sDFt.format(msg.getTimestamp());

        String content = "[暂不支持此消息类型]";
        if (msg instanceof AVIMTextMessage)
            content = ((AVIMTextMessage) msg).getText();

        contentView.setText(content);
        timeView.setText(time);

        if (AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == msg.getMessageStatus()) {
            errorView.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.GONE);
            statusView.setVisibility(View.VISIBLE);
        } else if (AVIMMessage.AVIMMessageStatus.AVIMMessageStatusSending == msg.getMessageStatus()) {
            errorView.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
            statusView.setVisibility(View.VISIBLE);
        } else
            statusView.setVisibility(View.GONE);
    }

    public void showTimeView(boolean isShow) {
        timeView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setNicknameAndAvatar(String nickname, byte[] avatar) {
        nicknameView.setText(nickname);
        if (avatar == null)
            avatarImg.setImageResource(R.drawable.me_avatar_null);
        else
            avatarImg.setImageBitmap(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
    }
}
