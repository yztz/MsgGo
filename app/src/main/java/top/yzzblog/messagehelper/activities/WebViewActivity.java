package top.yzzblog.messagehelper.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import java.nio.file.WatchEvent;

import top.yzzblog.messagehelper.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        //更换标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View actionbarView = LayoutInflater.from(this).inflate(R.layout.layout_web_action_bar, new ConstraintLayout(this), false);
            actionBar.setCustomView(actionbarView);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toolbar parent = (Toolbar) actionbarView.getParent();
                parent.setContentInsetsAbsolute(0, 0);
            }

            ImageView mImgQuit= actionbarView.findViewById(R.id.img_quit);
            mImgQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        mWv = findViewById(R.id.wv);
        mWv.getSettings().setJavaScriptEnabled(true);
        mWv.getSettings().setDomStorageEnabled(true);
        mWv.loadUrl("http://yzzblog.top/msggo/");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true;

        return super.onKeyDown(keyCode, event);
    }
}
