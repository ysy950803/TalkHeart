package com.ysy.talkheart.im;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.im.events.InputBottomBarEvent;
import com.ysy.talkheart.im.events.InputBottomBarTextEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class AVInputBottomBar extends LinearLayout {

    private final int MIN_INTERVAL_SEND_MESSAGE = 1000;
    private final int WORD_LIMIT = 144;

    @BindView(R.id.chat_input_send_img)
    ImageView sendBtn;
    @BindView(R.id.chat_input_edt)
    AppCompatEditText contentEdt;

    public AVInputBottomBar(Context context) {
        super(context);
        initView(context);
    }

    public AVInputBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(final Context context) {
        View.inflate(context, R.layout.input_bottom_bar, this);
        ButterKnife.bind(this);
        contentEdt.requestFocus();
        setEditTextChangeListener();

        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdt.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(context, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
                    return;
                }
                contentEdt.setText("");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendBtn.setEnabled(true);
                    }
                }, MIN_INTERVAL_SEND_MESSAGE);

                EventBus.getDefault().post(
                        new InputBottomBarTextEvent(InputBottomBarEvent.INPUTBOTTOMBAR_SEND_TEXT_ACTION, content, getTag()));
            }
        });
    }

    private void setEditTextChangeListener() {
        contentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = contentEdt.getText();
                int len = editable.length();
                if (len > WORD_LIMIT) {
                    int selectEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    String newStr = str.substring(0, WORD_LIMIT);
                    contentEdt.setText(newStr);
                    editable = contentEdt.getText();
                    int newLength = editable.length();
                    if (selectEndIndex > newLength) {
                        selectEndIndex = editable.length();
                        Toast.makeText(getContext(), "最多只能输入 " + selectEndIndex + " 个字哦！", Toast.LENGTH_SHORT).show();
                    }
                    Selection.setSelection(editable, selectEndIndex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}