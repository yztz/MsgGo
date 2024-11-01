package top.yzzblog.messagehelper.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.work.WorkManager;

import com.daimajia.numberprogressbar.NumberProgressBar;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.services.MessageService;
import top.yzzblog.messagehelper.util.Config;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.widgets.ObservableScrollView;

public class ProgressDialog extends Dialog {
    private NumberProgressBar mPb;
    private TextView mTvMsg, mTvTitle;
    private Button mBtnCancel;
    private ObservableScrollView mSv;
    private ImageView mImgClose;
    private Context context;
    private int max;
    private Handler handler = new Handler();


    public ProgressDialog(Context context, int max) {
        super(context);
        this.max = max;
        this.context = context;
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_pb);

        //设置宽度
        WindowManager m = getWindow().getWindowManager();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Display d = m.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        p.width = (int)(size.x * 0.8);
        getWindow().setAttributes(p);

        mPb = findViewById(R.id.pb);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mTvMsg = findViewById(R.id.tv_broadMsg);
        mTvTitle = findViewById(R.id.tv_title);
        mSv = findViewById(R.id.sv_broad);
        mImgClose = findViewById(R.id.img_close);
        mPb.setMax(max);

        mImgClose.setOnClickListener(v -> dismiss());

        mBtnCancel.setOnClickListener(v -> {
            context.stopService(new Intent(context, MessageService.class));
            ToastUtil.show(getContext(), "发送任务取消成功");
            dismiss();
        });
    }

    public void update(int progress) {
        if(progress < max)
            mPb.setProgress(progress);
        else{
            mPb.setProgress(progress);
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    dismiss();
//                }
//            }, 5000);
            mTvTitle.setText("发送完成");
            mImgClose.setVisibility(View.VISIBLE);
            mBtnCancel.setVisibility(View.GONE);
        }
    }

    public void appendMsg(String msg) {
        mTvMsg.append(msg);
        if(!mSv.isTouching()) mSv.toBottom();
    }
}