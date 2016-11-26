package com.ysy.talkheart.activities;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ViewTurnAnimation;

public class RegisterActivity extends AppCompatActivity {

    private ImageView backImg;
    private ImageView doneImg;
    private LinearLayout infoLayout;
    private EditText userEdt;
    private EditText pwEdt;
    private EditText rePwEdt;
    private EditText nicknameEdt;
    private TextView setBirthTv;
    private Switch sexSwitch;
    private CalendarView calendarView;

    private Handler registerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        clickListener();
        registerHandler = new Handler();
    }

    private void initView() {
        backImg = (ImageView) findViewById(R.id.register_back_img);
        doneImg = (ImageView) findViewById(R.id.register_done_img);
        infoLayout = (LinearLayout) findViewById(R.id.register_info_layout);
        userEdt = (EditText) findViewById(R.id.register_user_edt);
        pwEdt = (EditText) findViewById(R.id.register_pw_edt);
        rePwEdt = (EditText) findViewById(R.id.register_re_pw_edt);
        nicknameEdt = (EditText) findViewById(R.id.register_nickname_edt);
        setBirthTv = (TextView) findViewById(R.id.register_birthday_tv);
        sexSwitch = (Switch) findViewById(R.id.register_sex_switch);
        calendarView = (CalendarView) findViewById(R.id.register_calendarView);

        infoLayout.setVisibility(View.VISIBLE);
        calendarView.setVisibility(View.GONE);
    }

    private void clickListener() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        doneImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userEdt.getText().toString();
                String pw = pwEdt.getText().toString();
                String rePw = rePwEdt.getText().toString();
                String nickname = nicknameEdt.getText().toString();
                String birthday = setBirthTv.getText().toString();
                int sex = 0;

                register(user, pw, rePw, nickname, birthday, sex);
            }
        });

        final ViewTurnAnimation animation = new ViewTurnAnimation(infoLayout, calendarView);
        setBirthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                calendarView.setVisibility(View.VISIBLE);
//                infoLayout.setVisibility(View.GONE);
                infoLayout.startAnimation(animation.getSATo(0));
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                setBirthTv.setText(year + "-" + month + "-" + dayOfMonth);
//                view.setVisibility(View.GONE);
//                infoLayout.setVisibility(View.VISIBLE);
                calendarView.startAnimation(animation.getSATo(0));
            }
        });
    }

    private boolean register(final String username, final String pw, String rePw, final String nickname, final String birthday, final int sex) {
        if (username.equals("")) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (pw.equals("") || rePw.equals("")) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!pw.equals(rePw)) {
            Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        } else if (nickname.equals("")) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!birthday.contains("-")) {
            Toast.makeText(this, "请设置生日", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToRegister(username, pw, nickname, birthday, sex);
        return true;
    }

    private void connectToRegister(final String username, final String pw, final String nickname, final String birthday, final int sex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                dbP.getConn();
                int uid;
                if ((uid = dbP.rowSelect("select uid from user")) > -1) {
                    int res = dbP.insert(
                            "insert into user(uid, username, pw, nickname, birthday, sex) values(" +
                                    (uid + 1) + ", '" + username + "', '" + pw + "', '" + nickname +
                                    "', '" + birthday + "', " + sex + ")"
                    );
                    if (res == 1)
                        registerHandler.post(successRunnable);
                    else if (res == 2)
                        registerHandler.post(nameErrorRunnable);
                    else
                        registerHandler.post(serverErrorRunnable);
                } else
                    registerHandler.post(serverErrorRunnable);
                dbP.closeConn();
            }
        }).start();
    }

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RegisterActivity.this, "服务器连接出错，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable nameErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RegisterActivity.this, "用户名已存在，换一个吧！", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RegisterActivity.this, "恭喜你加入我们！", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };
}
