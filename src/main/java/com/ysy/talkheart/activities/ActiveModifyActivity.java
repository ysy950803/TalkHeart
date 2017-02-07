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
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;
import com.ysy.talkheart.utils.StringUtils;

public class ActiveModifyActivity extends DayNightActivity {

    private EditText modifyEdt;
    private TextView restWordTv;
    private static final int WORD_LIMIT = 144;
    private Handler modifyHandler;
    private ProgressDialog waitDialog;
    private String UID;
    private String ACT_ID = "";
    private String MODIFY_CONTENT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_modify);
        setupActionBar(true);
        initData();
        initView();
        modifyHandler = new Handler();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        ACT_ID = getIntent().getExtras().getString("actid");
        MODIFY_CONTENT = getIntent().getExtras().getString("modify_content", "");
    }

    private void initView() {
        modifyEdt = (EditText) findViewById(R.id.active_modify_edt);
        restWordTv = (TextView) findViewById(R.id.active_modify_word_tv);
        modifyEdt.addTextChangedListener(tw);
        modifyEdt.setText(MODIFY_CONTENT);
    }

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d("TEST", "beforeTC:\n" + "start: " + start + "\nafter:" + after + "\ncount" + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d("TEST", "onTC:\n" + "start: " + start + "\nbefore:" + before + "\ncount" + count);
            Editable editable = modifyEdt.getText();
            int len = editable.length();
            restWordTv.setText("还能输入" + (WORD_LIMIT - len) + "个字");
            if (len > WORD_LIMIT) {
                int selectEndIndex = Selection.getSelectionEnd(editable); // getSelectionEnd获取光标结束的索引值
                String str = editable.toString(); // 旧字符串
                String newStr = str.substring(0, WORD_LIMIT); // 截取新字符串
                modifyEdt.setText(newStr);
                editable = modifyEdt.getText();
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

    private boolean send(final String actid, final String content) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToModify(actid, content);
        return true;
    }

    private boolean save(final int uid, final String content) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToSave(uid, content);
        return true;
    }

    private void connectToModify(final String actid, final String content) {
        waitDialog = ProgressDialog.show(ActiveModifyActivity.this, "请稍后", "正在和数据库君吃饭……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    modifyHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.update(
                            "update active set content = '" + content + "' where actid = " + actid
                    );
                    if (res == 1)
                        modifyHandler.post(modifyRunnable);
                    else
                        modifyHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private void connectToSave(final int uid, final String content) {
        waitDialog = ProgressDialog.show(ActiveModifyActivity.this, "请稍后", "正在请数据库君吃饭……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    modifyHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.insert(
                            "insert into draft(uid, savetime, content) values(" +
                                    uid + ", NOW(), '" + content + "')"
                    );
                    if (res == 1)
                        modifyHandler.post(saveRunnable);
                    else
                        modifyHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private Runnable modifyRunnable = new Runnable() {
        @Override
        public void run() {
            ((GlobalApp) getApplication()).setHomeActiveUpdated(true);
            Toast.makeText(ActiveModifyActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveModifyActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveModifyActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ActiveModifyActivity.this, "成功保存到草稿箱啦", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                String modifyContent = modifyEdt.getText().toString();
                modifyContent = StringUtils.zipBlank(modifyContent);
                if (!StringUtils.replaceBlank(modifyContent).equals(""))
                    send(ACT_ID, modifyContent);
                else
                    Toast.makeText(ActiveModifyActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            }
        });

        menu.findItem(R.id.action_save).setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                String modifyContent = modifyEdt.getText().toString();
                modifyContent = StringUtils.zipBlank(modifyContent);
                if (!StringUtils.replaceBlank(modifyContent).equals(""))
                    save(Integer.parseInt(UID), modifyContent);
                else
                    Toast.makeText(ActiveModifyActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }
}
