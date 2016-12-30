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
import com.ysy.talkheart.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedbackActivity extends AppCompatActivity {

    private static final int WORD_LIMIT = 144;
    private TextView restWordTv;
    private EditText writeEdt;
    private String UID;
    private Handler feedbackHandler;
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setupActionBar();
        initData();
        initView();
        feedbackHandler = new Handler();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
    }

    private void initView() {
        writeEdt = (EditText) findViewById(R.id.feedback_write_edt);
        restWordTv = (TextView) findViewById(R.id.feedback_write_word_tv);

        writeEdt.addTextChangedListener(tw);
    }

    private boolean send(final int uid, final String content) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        waitDialog = ProgressDialog.show(FeedbackActivity.this, "请稍后", "正在疯狂吐槽开发君……");
        connectToSend(uid, content);
        return true;
    }

    private void connectToSend(final int uid, final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn() == null) {
                    feedbackHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.insert(
                            "insert into feedback(uid, sendtime, content) values(" +
                                    uid + ", NOW(), '" + content + "')"
                    );
                    if (res == 1)
                        feedbackHandler.post(sendRunnable);
                    else
                        feedbackHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

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

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FeedbackActivity.this, "感谢反馈，我们会做得更好", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FeedbackActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(FeedbackActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
//                String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                String writeContent = writeEdt.getText().toString();
                if (!StringUtils.replaceBlank(writeContent).equals(""))
                    send(Integer.parseInt(UID), writeContent);
                else
                    Toast.makeText(FeedbackActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
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
