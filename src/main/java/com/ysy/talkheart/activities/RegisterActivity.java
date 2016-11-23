package com.ysy.talkheart.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ysy.talkheart.R;

public class RegisterActivity extends AppCompatActivity {

    private ImageView backImg;
    private ImageView doneImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        clickListener();
    }

    private void initView() {
        backImg = (ImageView) findViewById(R.id.register_back_img);
        doneImg = (ImageView) findViewById(R.id.register_done_img);
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

                onBackPressed();
            }
        });
    }
}
