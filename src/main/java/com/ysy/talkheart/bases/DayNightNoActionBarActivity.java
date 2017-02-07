package com.ysy.talkheart.bases;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ysy.talkheart.R;
import com.ysy.talkheart.utils.DataProcessor;

public class DayNightNoActionBarActivity extends AppCompatActivity {

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
            setTheme(R.style.AppThemeNight_NoActionBar);
        } else { // day
            setTheme(R.style.AppTheme_NoActionBar);
        }
    }

    protected void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
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
