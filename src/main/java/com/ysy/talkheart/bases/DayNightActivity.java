package com.ysy.talkheart.bases;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.DataProcessor;

public class DayNightActivity extends AppCompatActivity {

    protected int dayNight = 0;
    protected String[] opts_o;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getExtras() != null)
            opts_o = getIntent().getExtras().getStringArray("opts_o");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        initTheme();
        super.setContentView(layoutResID);
    }

    private void initTheme() {
        DataProcessor dP = new DataProcessor(this);
        if (dP.readIntData("day_night") == 1) { // night
            dayNight = 1;
            setTheme(R.style.AppThemeNight);
        } else { // day
            dayNight = 0;
            setTheme(R.style.AppTheme);
        }
    }

    protected void setupActionBar(boolean isShowCloseIcon) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (isShowCloseIcon)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
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
}
