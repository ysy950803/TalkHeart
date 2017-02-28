package com.ysy.talkheart.im.events;

public class InputBottomBarTextEvent extends InputBottomBarEvent {

    public String sendContent;

    public InputBottomBarTextEvent(int action, String content, Object tag) {
        super(action, tag);
        sendContent = content;
    }
}
