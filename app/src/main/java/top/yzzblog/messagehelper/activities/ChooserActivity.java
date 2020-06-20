package top.yzzblog.messagehelper.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.adapters.ListAdapter;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.dialog.ProgressDialog;
import top.yzzblog.messagehelper.services.MessageService;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.ToastUtil;

public class ChooserActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private LinearLayout mLinearSend;
    private ProgressDialog pro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        mRv = findViewById(R.id.rv_data);
        mLinearSend = findViewById(R.id.linear_send);
        registerReceiver();

        final RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRv.setLayoutManager(manager);
        final ListAdapter adapter = new ListAdapter(this);
        mRv.setAdapter(adapter);

        mLinearSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送消息
                ArrayList<Integer> itemIndices = new ArrayList<>();
                SparseBooleanArray checkedMap = adapter.getCheckedMap();
                int max_limit = DataLoader.getMaxLimit();
                //获取选中项目索引
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (checkedMap.get(i)) {
                        itemIndices.add(i);
                    }
                }
                //小于等于0表示无限制
                if (itemIndices.size() == 0) {
                    ToastUtil.show(ChooserActivity.this, "当前还未选择任何收件人哦~");
                    return;
                }

                if (max_limit <= 0 || itemIndices.size() <= max_limit) {
                    pro = new ProgressDialog(ChooserActivity.this, itemIndices.size());
                    pro.show();
                    Intent intent = new Intent(ChooserActivity.this, MessageService.class);
                    intent.putExtra("itemIndices", itemIndices);
                    startService(intent);
                } else {
                    ToastUtil.show(ChooserActivity.this, "超出了最大人数限制" + max_limit + "了哦~");
                }

            }
        });

        //更换标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View actionbarView = LayoutInflater.from(this).inflate(R.layout.layout_chooser_action_bar, new ConstraintLayout(this), false);
            actionBar.setCustomView(actionbarView);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toolbar parent = (Toolbar) actionbarView.getParent();
                parent.setContentInsetsAbsolute(0, 0);
            }

            final TextView mTvChooseAll = actionbarView.findViewById(R.id.tv_chooseAll);
            mTvChooseAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTvChooseAll.getText().equals("全选")) {
                        adapter.setAllCheckBoxChosen(true);
                        mTvChooseAll.setText("取消全选");
                    } else {
                        adapter.setAllCheckBoxChosen(false);
                        mTvChooseAll.setText("全选");
                    }
                }
            });
        }

        //选择号码列
        DataLoader.askNumberColumn(this);

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SMSSender.SENT_SMS_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int code = intent.getIntExtra("code", -1);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        pro.appendMsg("第" + code + "条 发送成功\n");
                        pro.update(code);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                    case SmsManager.RESULT_ERROR_RADIO_OFF:

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        pro.appendMsg("第" + code + "条 发送失败\n");
                        pro.update(code);
                        break;
                }
            }
        }, filter);

        //test
//        filter = new IntentFilter(MessageService.TEST);
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                int code = intent.getIntExtra("code", -1);
//                String content = intent.getStringExtra("content");
//                String number = intent.getStringExtra("number");
//                pro.update(code);
//                pro.appendMsg("第" + code + "条 发送成功\n");
//                Log.d("msgD", "code= " + code + " content= " + content + " number= " + number);
//            }
//        }, filter);
    }
}
