package com.ysy.talkheart.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Shengyu Yao on 2016/12/29.
 */

public class StringUtils {

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static boolean isHavingBlank(String str) {
        String dest;
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
            return str.length() > dest.length();
        }
        return true;
    }
}
