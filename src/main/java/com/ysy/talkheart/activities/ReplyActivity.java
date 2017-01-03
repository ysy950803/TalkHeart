package com.ysy.talkheart.activities;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;

public class ReplyActivity extends AppCompatActivity {

    private static final int WORD_LIMIT = 72;
    private TextView restWordTv;
    private EditText writeEdt;
    private String UID;
    private String E_UID;
    private String ACT_ID;
    private String CMT_ID;
    private String NICKNAME;
    private Handler replyHandler;
    private ProgressDialog waitDialog;
    private int SEND_MODE; // 0:error 1:comment 2:reply

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        setupActionBar();
        replyHandler = new Handler();
        initData();
        initView();
    }

    private boolean initData() {
        UID = getIntent().getExtras().getString("uid");
        E_UID = getIntent().getExtras().getString("e_uid");
        ACT_ID = getIntent().getExtras().getString("actid");
        CMT_ID = getIntent().getExtras().getString("cmtid");
        NICKNAME = getIntent().getExtras().getString("nickname");
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

    private boolean send(int send_mode, int e_uid, final int uid, final String content) {
        if (content.equals("")) {
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
        if (send_mode == 0)
            Toast.makeText(this, "出现未知错误，请返回重试", Toast.LENGTH_SHORT).show();
        else if (send_mode == 1) { // comment
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBProcessor dbP = new DBProcessor();
                    if (dbP.getConn() == null) {
                        replyHandler.post(timeOutRunnable);
                    } else {
                        int res = dbP.insert(
                                "insert into comment(uid, actid, content, sendtime, uid_p) values(" +
                                        e_uid + ", " + ACT_ID + ", '" + content + "', NOW(), " + uid + ")"
                        );
                        if (res == 1) {
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
                    if (dbP.getConn() == null) {
                        replyHandler.post(timeOutRunnable);
                    } else {
                        int res = dbP.insert(
                                "insert into comment(uid, actid, content, sendtime, uid_p, cmtid_p) values(" +
                                        e_uid + ", " + ACT_ID + ", '回复 " + NICKNAME + "：" + content + "', NOW(), " +
                                        uid + ", " + (CMT_ID.equals("") ? "-1" : CMT_ID) + ")"
                        );
                        if (res == 1) {
                            dbP.update("update user set isread = 0 where uid = " + uid);
                            replyHandler.post(sendRunnable);
                        } else
                            replyHandler.post(serverErrorRunnable);
                    }
                    dbP.closeConn();
                    waitDialog.dismiss();
                }
            }).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                send(SEND_MODE, Integer.parseInt(E_UID), Integer.parseInt(UID), writeEdt.getText().toString());
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ReplyActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
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
}
