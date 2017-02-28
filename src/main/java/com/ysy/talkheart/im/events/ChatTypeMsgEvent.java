package com.ysy.talkheart.im.events;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

public class ChatTypeMsgEvent {
    public AVIMTypedMessage msg;
    public AVIMConversation conv;
}
