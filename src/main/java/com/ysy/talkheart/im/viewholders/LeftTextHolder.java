package com.ysy.talkheart.im.viewholders;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.ysy.talkheart.R;
import com.ysy.talkheart.im.events.LeftChatItemClickEvent;
import com.ysy.talkheart.views.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LeftTextHolder extends ChatViewHolder {

    @BindView(R.id.single_chat_left_time)
    TextView timeView;

    @BindView(R.id.single_chat_left_text)
    TextView contentView;

    @BindView(R.id.single_chat_left_name)
    TextView nicknameView;

    @BindView(R.id.single_chat_leaft_avatar)
    CircularImageView avatarImg;

    public LeftTextHolder(Context context, ViewGroup root) {
        super(context, root, R.layout.item_single_chat_left);
    }

    @OnClick({R.id.single_chat_left_text, R.id.single_chat_left_name})
    void onNicknameClick(View v) {
        LeftChatItemClickEvent clickEvent = new LeftChatItemClickEvent();
        clickEvent.nickname = nicknameView.getText().toString();
        EventBus.getDefault().post(clickEvent);
    }

    @Override
    public void bindData(Object o) {
        AVIMMessage msg = (AVIMMessage) o;
        SimpleDateFormat sDFt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String time = sDFt.format(msg.getTimestamp());

        String content = "[暂不支持此消息类型]";
        if (msg instanceof AVIMTextMessage) {
            content = ((AVIMTextMessage) msg).getText();
        }

        contentView.setText(content);
        timeView.setText(time);
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
