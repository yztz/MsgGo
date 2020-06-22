package top.yzzblog.messagehelper.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.Timer;

import top.yzzblog.messagehelper.R;

public class CoverActivity extends AppCompatActivity {
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.hide();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(CoverActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, R.anim.fade_out);
            }
        }, 1800);
    }
}
