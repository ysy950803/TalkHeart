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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WriteActivity extends AppCompatActivity {

    private EditText writeEdt;
    private TextView restWordTv;
    private static final int WORD_LIMIT = 144;
    private Handler sendHandler;
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        setupActionBar();
        initView();
        sendHandler = new Handler();
    }

    private void initView() {
        writeEdt = (EditText) findViewById(R.id.write_edt);
        restWordTv = (TextView) findViewById(R.id.write_word_tv);

        writeEdt.addTextChangedListener(tw);
    }

    private boolean send(final int uid, final String sendTime, final String content) {
        if (content.equals("")) {
            Toast.makeText(this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        waitDialog = ProgressDialog.show(WriteActivity.this, "请稍后", "正在请数据库君吃饭……");
        connectToSend(uid, sendTime, content);
        return true;
    }

    private void connectToSend(final int uid, final String sendTime, final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                dbP.getConn();
                int actid;
                if ((actid = dbP.rowSelect("select actid from active")) >= -1) {
                    int res = dbP.insert(
                            "insert into active(uid, actid, sendtime, goodnum, content) values(" +
                                    uid + ", " + (actid + 1) + ", '" + sendTime + "', 0, '" + content + "')"
                    );
                    if (res == 1)
                        sendHandler.post(successRunnable);
                    else
                        sendHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WriteActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WriteActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d("TEST", "beforeTC:\n" + "start: " + start + "\nafter:" + after + "\ncount" + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d("TEST", "onTC:\n" + "start: " + start + "\nbefore:" + before + "\ncount" + count);
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
//                    Toast.makeText(WriteActivity.this, "最多只能输入" + selectEndIndex + "个字哦！", Toast.LENGTH_SHORT).show();
                }
                Selection.setSelection(editable, selectEndIndex); // 设置新光标所在的位置
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                send(0, sendTime, writeEdt.getText().toString());
                return true;
            }
        });

        menu.findItem(R.id.action_save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // save to draft

                return true;
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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }
}