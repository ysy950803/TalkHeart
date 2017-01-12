package com.ysy.talkheart.bases;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.DataProcessor;

public class DayNightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
    }

    protected void initTheme() {
        DataProcessor dP = new DataProcessor(this);
        if (dP.readIntData("day_night") == 1) { // night
            setTheme(R.style.AppThemeNight);
        } else { // day
            setTheme(R.style.AppTheme);
        }
    }
}
