package com.ysy.talkheart.im;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ysy.talkheart.activities.WelcomeActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.im.activities.HomeActivity;
import com.ysy.talkheart.im.activities.SingleChatActivity;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        if (id != -1) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
            ((GlobalApp) context.getApplicationContext()).decNTFCount();
        }

        String action = intent.getAction();
        if (action.equals("clicked")) {
            if (ChatClientManager.getInstance().getClient() == null)
                gotoInitActivity(context);
            else {
                String conv_id = intent.getStringExtra(ChatConstants.CONV_ID);
                if (!TextUtils.isEmpty(conv_id))
                    gotoSingleChatActivity(context, intent);
                else
                    gotoHomeChatList(context, intent);
            }
        }
    }

    private void gotoInitActivity(Context context) {
        Intent intentW = new Intent(context, WelcomeActivity.class);
        intentW.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentW);
    }

    private void gotoSingleChatActivity(Context context, Intent intent) {
        Intent intentC = new Intent(context, SingleChatActivity.class);
        intentC.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentC.putExtra("me_uid", intent.getStringExtra("me_uid"));
        intentC.putExtra("me_nickname", intent.getStringExtra("me_nickname"));
        intentC.putExtra("me_avatar", intent.getByteArrayExtra("me_avatar"));
        intentC.putExtra(ChatConstants.OBJ_ID, intent.getStringExtra(ChatConstants.OBJ_ID));
        intentC.putExtra("obj_nickname", intent.getStringExtra("obj_nickname"));
        intentC.putExtra("obj_avatar", intent.getByteArrayExtra("obj_avatar"));
        context.startActivity(intentC);
    }

    private void gotoHomeChatList(Context context, Intent intent) {
        Intent intentC = new Intent(context, HomeActivity.class);
        intentC.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentC.putExtra("to_home", true);
        intentC.putExtra("me_uid", intent.getStringExtra("me_uid"));
        context.startActivity(intentC);
    }
}
