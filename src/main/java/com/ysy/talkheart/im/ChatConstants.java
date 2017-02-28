package com.ysy.talkheart.im;

public class ChatConstants {

    private static final String PREFIX = "com.ysy.talkheart";

    public static final String OBJ_ID = getPrefixConstant("obj_id");
    static final String CONV_ID = getPrefixConstant("conv_id");

    private static String getPrefixConstant(String str) {
        return PREFIX + str;
    }
}
