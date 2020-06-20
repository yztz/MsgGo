package top.yzzblog.messagehelper.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import top.yzzblog.messagehelper.R;

public class LoadDialog extends Dialog {
    private TextView mTvLoading;

    public LoadDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.layout_loading);

        //设置宽度
        WindowManager m = getWindow().getWindowManager();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Display d = m.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        p.width = (int)(size.x * 0.8);
        getWindow().setAttributes(p);

        mTvLoading = findViewById(R.id.tv_loading);
    }


}
