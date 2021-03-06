package com.ysy.talkheart.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.DayNightFullScreenActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.StringUtils;

import butterknife.BindView;

public class PersonModifyActivity extends DayNightFullScreenActivity {

    @BindView(R.id.modify_back_img)
    ImageView backImg;
    @BindView(R.id.modify_done_img)
    ImageView doneImg;
    @BindView(R.id.modify_school_edt)
    EditText schoolEdt;
    @BindView(R.id.modify_old_pw_edt)
    EditText oldPwEdt;
    @BindView(R.id.modify_new_pw_edt)
    EditText newPwEdt;
    @BindView(R.id.modify_re_new_pw_edt)
    EditText reNewPwEdt;
    @BindView(R.id.modify_nickname_edt)
    EditText nicknameEdt;
    @BindView(R.id.modify_birthday_tv)
    TextView setBirthTv;
    @BindView(R.id.modify_sex_switch)
    Switch sexSwitch;
    private String SCHOOL;
    private String NICKNAME;
    private String BIRTHDAY;
    private int YEAR = 1995;
    private int MONTH = 8;
    private int DAY = 3;
    private int SEX;
    private String UID;
    private Handler modifyHandler;
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_modify);
        initData();
        initView();
        clickListener();
        modifyHandler = new Handler();
    }

    private void initData() {
        UID = getIntent().getExtras().getString("uid");
        SCHOOL = getIntent().getExtras().getString("school");
        NICKNAME = getIntent().getExtras().getString("nickname");
        BIRTHDAY = getIntent().getExtras().getString("birthday");
        SEX = getIntent().getExtras().getInt("sex");
        getYMD(BIRTHDAY);
    }

    private void initView() {
        newPwEdt.addTextChangedListener(tw);
        schoolEdt.setText(SCHOOL);
        nicknameEdt.setText(NICKNAME);
        setBirthTv.setText(BIRTHDAY);
        sexSwitch.setChecked(SEX == 0);
        reNewPwEdt.setVisibility(View.GONE);
    }

    private void clickListener() {
        backImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                onBackPressed();
            }
        });

        doneImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                String school = schoolEdt.getText().toString();
                String oldPw = oldPwEdt.getText().toString();
                String newPw = newPwEdt.getText().toString();
                String reNewPw = reNewPwEdt.getText().toString();
                String nickname = nicknameEdt.getText().toString();
                String birthday = setBirthTv.getText().toString();
                modify(school, oldPw, newPw, reNewPw, nickname, birthday, SEX);
            }
        });

        sexSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    SEX = 0;
                else
                    SEX = 1;
            }
        });

        setBirthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PersonModifyActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        setBirthTv.setText(formatDate(year + "-" + (month + 1) + "-" + dayOfMonth));
                    }
                }, YEAR, MONTH - 1, DAY).show();
            }
        });
    }

    private boolean modify(String school, String oldPw, String newPw, String reNewPw, String nickname,
                           String birthday, int sex) {
        if (StringUtils.replaceBlank(school).equals(""))
            school = "";
        if (StringUtils.isHavingBlank(newPw) || StringUtils.isHavingBlank(reNewPw)) {
            Toast.makeText(this, "新密码不能包含空格哦", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!newPw.equals(reNewPw)) {
            Toast.makeText(this, "两次输入新密码不一样呢", Toast.LENGTH_SHORT).show();
            return false;
        } else if (StringUtils.replaceBlank(nickname).equals("")) {
            Toast.makeText(this, "要有昵称哦", Toast.LENGTH_SHORT).show();
            return false;
        } else if (StringUtils.replaceBlank(oldPw).equals("")) {
            Toast.makeText(this, "旧密码不能为空哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (newPw.length() > 24 || reNewPw.length() > 24) {
            Toast.makeText(this, "新密码不能超过24个字符哦", Toast.LENGTH_SHORT).show();
            return false;
        } else if (nickname.length() > 24) {
            Toast.makeText(this, "昵称不能超过24个字符哦", Toast.LENGTH_SHORT).show();
            return false;
        } else if (nickname.length() > 24 && StringUtils.getChineseCount(nickname) > 12) {
            Toast.makeText(this, "昵称不能包含超过12个汉字哦", Toast.LENGTH_SHORT).show();
            return false;
        } else if (school.length() > 16) {
            Toast.makeText(this, "学校不能超过16个字符哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        waitDialog = ProgressDialog.show(PersonModifyActivity.this, "请稍后", "服务器君正在开空调……");
        connectToModify(school, oldPw, newPw, nickname, birthday, sex);
        return true;
    }

    private void connectToModify(final String school, final String oldPw, final String newPw, final String nickname,
                                 final String birthday, final int sex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null)
                    modifyHandler.post(timeOutRunnable);
                else {
                    String pw = dbP.pwSelect("select pw from user where uid = " + UID);
                    if (pw != null) {
                        if (pw.equals(oldPw)) {
                            int res1 = dbP.update("update user set school = '" + (school.equals("") ? "未设置院校" : school) + "', pw = '" + (newPw.equals("") ? oldPw : newPw) + "', nickname = '" +
                                    nickname + "', birthday = '" + birthday + "', sex = " + sex + " where uid = " + UID);
                            if (res1 == 1)
                                modifyHandler.post(successRunnable);
                            else
                                modifyHandler.post(serverErrorRunnable);
                        } else
                            modifyHandler.post(pwErrorRunnable);
                    } else
                        modifyHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private void getYMD(String date) {
        String[] yMd = date.split("-");
        String year = yMd[0];
        String month = yMd[1];
        String day = yMd[2];
        YEAR = Integer.parseInt(year);
        MONTH = Integer.parseInt(month);
        DAY = Integer.parseInt(day);
    }

    private String formatDate(String date) {
        String[] yMd = date.split("-");
        String year = yMd[0];
        String month = yMd[1];
        String day = yMd[2];
        YEAR = Integer.parseInt(year);
        MONTH = Integer.parseInt(month);
        DAY = Integer.parseInt(day);
        if (MONTH < 10)
            month = "0" + month;
        if (DAY < 10)
            day = "0" + day;
        return yMd[0] + "-" + month + "-" + day;
    }

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d("TEST", "beforeTC:\n" + "start: " + start + "\nafter:" + after + "\ncount" + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Editable editable = newPwEdt.getText();
            int len = editable.length();
            if (len > 0) {
                reNewPwEdt.setVisibility(View.VISIBLE);
            } else {
                reNewPwEdt.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonModifyActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            GlobalApp app = (GlobalApp) getApplication();
            app.setMeInfoUpdated(true);
            Toast.makeText(PersonModifyActivity.this, "修改成功啦", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonModifyActivity.this, "服务器君发脾气了，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable pwErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(PersonModifyActivity.this, "旧密码不正确哦，请重试", Toast.LENGTH_SHORT).show();
        }
    };
}
