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

    static void showNotification(Context context, String title, String content, Intent clickIntent) {
        ((GlobalApp) context).setNotificationShown(true);
        ((GlobalApp) context).addNTFCount();
        int id = (new Random()).nextInt();

        clickIntent.setAction("clicked");
        clickIntent.putExtra("id", id);
        PendingIntent clickPending = PendingIntent.getBroadcast(context, id, clickIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent delIntent = new Intent(context, NotificationBroadcastReceiver.class);
        delIntent.setAction("deleted");
        delIntent.putExtra("id", id);
        PendingIntent delPending = PendingIntent.getBroadcast(context, id, delIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentIntent(clickPending)
                        .setDeleteIntent(delPending);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.vibrate = null;
        notification.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/" + R.raw.pop);
        manager.notify(id, notification);
    }
}
