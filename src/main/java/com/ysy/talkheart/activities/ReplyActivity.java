package com.ysy.talkheart.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;
import com.ysy.talkheart.utils.StringUtils;

public class ReplyActivity extends DayNightActivity {

    private static final int WORD_LIMIT = 72;
    private TextView restWordTv;
    private EditText writeEdt;
    private String UID;
    private String E_UID;
    private String ACT_ID;
    private String CMT_ID;
    private Handler replyHandler;
    private ProgressDialog waitDialog;
    private int SEND_MODE; // 0:error 1:comment 2:reply 3:modify
    private String CONTENT, NICKNAME_P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        setupActionBar(true);
        replyHandler = new Handler();
        initData();
        initView();
    }

    private boolean initData() {
        UID = getIntent().getExtras().getString("uid");
        E_UID = getIntent().getExtras().getString("e_uid");
        ACT_ID = getIntent().getExtras().getString("actid");
        CMT_ID = getIntent().getExtras().getString("cmtid");
        CONTENT = getIntent().getExtras().getString("modify_content");
        NICKNAME_P = getIntent().getExtras().getString("nickname_p");
        if (CONTENT != null) {
            SEND_MODE = 3;
            return true;
        }
        if (UID == null || E_UID == null || ACT_ID == null) {
            SEND_MODE = 0;
            return false;
        } else {
            if (CMT_ID == null) {
                SEND_MODE = 1;
            } else {
                SEND_MODE = 2;
            }
        }
        return true;
    }

    private void initView() {
        writeEdt = (EditText) findViewById(R.id.reply_write_edt);
        restWordTv = (TextView) findViewById(R.id.reply_write_word_tv);
        writeEdt.addTextChangedListener(tw);
        if (CONTENT != null)
            writeEdt.setText(CONTENT);
        writeEdt.setHint("正在吐槽 " + NICKNAME_P + " ……");
    }

    private boolean send(int send_mode, int e_uid, final int uid, String content) {
        content = StringUtils.zipBlank(content);
        if (StringUtils.replaceBlank(content).equals("")) {
            Toast.makeText(this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        waitDialog = ProgressDialog.show(ReplyActivity.this, "请稍后", "正在抽服务器君鞭子……");
        connectToSend(send_mode, e_uid, uid, content);
        return true;
    }

    private void connectToSend(final int send_mode, final int e_uid, final int uid, final String content) {
        if (send_mode == 0) {
            Toast.makeText(this, "出现未知错误，请返回重试", Toast.LENGTH_SHORT).show();
            waitDialog.dismiss();
        } else if (send_mode == 1) { // comment
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn(opts_o) == null) {
                        replyHandler.post(timeOutRunnable);
                    } else {
                        int res = dbP.insert(
                                "insert into comment(uid, actid, content, sendtime, uid_p) values(" +
                                        e_uid + ", " + ACT_ID + ", '" + content + "', NOW(), " + uid + ")"
                        );
                        if (res == 1) {
                            dbP.update("update active set cmtnum = (cmtnum + 1) where actid = " + ACT_ID);
                            if (e_uid != uid)
                                dbP.update("update user set isread = 0 where uid = " + uid);
                            replyHandler.post(sendRunnable);
                        } else
                            replyHandler.post(serverErrorRunnable);
                    }
                    dbP.closeConn();
                    waitDialog.dismiss();
                }
            }).start();
        } else if (send_mode == 2) { // reply
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn(opts_o) == null) {
                        replyHandler.post(timeOutRunnable);
                    } else {
                        int res = dbP.insert(
                                "insert into comment(uid, actid, content, sendtime, uid_p, cmtid_p) values(" +
                                        e_uid + ", " + ACT_ID + ", '" + content + "', NOW(), " +
                                        uid + ", " + (CMT_ID.equals("") ? "-1" : CMT_ID) + ")"
                        );
                        if (res == 1) {
                            dbP.doubleUpdate("update user set isread = 0 where uid = " + uid,
                                    "update active set cmtnum = (cmtnum + 1) where actid = " + ACT_ID);
                            replyHandler.post(sendRunnable);
                        } else
                            replyHandler.post(serverErrorRunnable);
                    }
                    dbP.closeConn();
                    waitDialog.dismiss();
                }
            }).start();
        } else if (send_mode == 3) { // modify
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn(opts_o) == null) {
                        replyHandler.post(timeOutRunnable);
                    } else {
                        int res = dbP.update(
                                "update comment set content = '" + content + "' where cmtid = " + CMT_ID
                        );
                        if (res == 1)
                            replyHandler.post(modifyRunnable);
                        else
                            replyHandler.post(serverErrorRunnable);
                    }
                    dbP.closeConn();
                    waitDialog.dismiss();
                }
            }).start();
        }
    }

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d("TEST", "beforeTC:\n" + "start: " + start + "\nafter:" + after + "\ncount" + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Editable editable = writeEdt.getText();
            int len = editable.length();
            restWordTv.setText("还能输入" + (WORD_LIMIT - len) + "个字");
            if (len > WORD_LIMIT) {
                int selectEndIndex = Selection.getSelectionEnd(editable); // getSelectionEnd获取光标结束的索引值
                String str = editable.toString(); // 旧字符串
                String newStr = str.substring(0, WORD_LIMIT); // 截取新字符串
                writeEdt.setText(newStr);
                editable = writeEdt.getText();
                int newLength = editable.length(); // 新字符串长度
                if (selectEndIndex > newLength) { // 如果光标结束的索引值超过新字符串长度
                    selectEndIndex = editable.length();
                }
                Selection.setSelection(editable, selectEndIndex); // 设置新光标所在的位置
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ReplyActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable modifyRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ReplyActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ReplyActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ReplyActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                String writeContent = writeEdt.getText().toString();
                writeContent = StringUtils.zipBlank(writeContent);
                if (!StringUtils.replaceBlank(writeContent).equals(""))
                    send(SEND_MODE, Integer.parseInt(E_UID), Integer.parseInt(UID), writeContent);
                else
                    Toast.makeText(ReplyActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }
}
