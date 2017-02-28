package com.ysy.talkheart.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.avos.avospush.notification.NotificationCompat;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.GlobalApp;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class NotificationUtils {

    private static List<String> notificationTagList = new LinkedList<>();

    public static void addTag(String tag) {
        if (!notificationTagList.contains(tag))
            notificationTagList.add(tag);
    }

    public static void removeTag(String tag) {
        notificationTagList.remove(tag);
    }

    static boolean isShowNotification(String tag) {
        return !notificationTagList.contains(tag);
    }

    static void showNotification(Context context, String title, String content, String sound, Intent intent) {
        ((GlobalApp) context).setNotificationShown(true);
        ((GlobalApp) context).addNTFCount();
        intent.setFlags(0);
        int id = (new Random()).nextInt();
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title).setAutoCancel(true).setContentIntent(contentIntent)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentText(content);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        if (sound != null && sound.trim().length() > 0)
            notification.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + sound);
        manager.notify(id, notification);
    }
}
