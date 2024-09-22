package top.yzzblog.messagehelper.activities;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.poi.util.ArrayUtil;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.adapters.ListAdapter;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.data.Message;
import top.yzzblog.messagehelper.dialog.ProgressDialog;
import top.yzzblog.messagehelper.services.MessageWorker;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.Config;
import top.yzzblog.messagehelper.util.FileUtil;
import top.yzzblog.messagehelper.util.TextParser;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.util.Utils;

public class ChooserActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private Button mSend;
    private ProgressDialog pro;
    private BroadcastReceiver receiver;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        mRv = findViewById(R.id.rv_data);
        mSend = findViewById(R.id.btn_send);
        topAppBar = findViewById(R.id.topAppBar);
        registerReceiver();

        final RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRv.setLayoutManager(manager);
        final ListAdapter adapter = new ListAdapter(this);
        mRv.setAdapter(adapter);

        mSend.setOnClickListener(v -> {
            //发送消息
            ArrayList<Integer> itemIndices = new ArrayList<>();
            SparseBooleanArray checkedMap = adapter.getCheckedMap();
//                int max_limit = DataLoader.getMaxLimit();
            //获取选中项目索引
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (checkedMap.get(i)) {
                    itemIndices.add(i);
                }
            }

            if (itemIndices.isEmpty()) {
                ToastUtil.show(ChooserActivity.this, "当前还未选择任何收件人哦~");
                return;
            }


            pro = new ProgressDialog(ChooserActivity.this, itemIndices.size());
            pro.show();

            String rawContent = DataLoader.getContent();
            String numberCol = DataLoader.getNumberColumn();

            // generate messages
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < itemIndices.size(); i++) {
                Map<String, String> tmp = DataLoader.getDataModel().getMap(itemIndices.get(i));
                String content = TextParser.parse(rawContent, tmp);
                String phoneNumber = tmp.get(numberCol);
                messages.add(new Message(phoneNumber, content));
            }

            // write to file
            String serPath = FileUtil.saveMessageArrayToFile(this, messages.toArray(new Message[0]));
            if (serPath == null) {
                ToastUtil.show(ChooserActivity.this, "短信服务启动失败");
                return;
            }

            Data inputData = new Data.Builder()
                    .putInt("delay", DataLoader.getDelay())
                    .putInt("subId", DataLoader.getSimSubId())
                    .putString("message_file", serPath)
                    .build();

            OneTimeWorkRequest messageWorkRequest = new OneTimeWorkRequest.Builder(MessageWorker.class)
                    .setInputData(inputData).addTag(Config.SEND_WORKER_TAG)
                    .build();
            WorkManager.getInstance(ChooserActivity.this).enqueue(messageWorkRequest);
//                } else {
//                    ToastUtil.show(ChooserActivity.this, "超出了最大人数限制" + max_limit + "了哦~");
//                }

        });


        topAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.select_all:
                    if ("全选".contentEquals(item.getTitle())) {
                        adapter.setAllCheckBoxChosen(true);
                        item.setTitle("取消全选");
                    } else {
                        adapter.setAllCheckBoxChosen(false);
                        item.setTitle("全选");
                    }
                    return true;
            }
            return false;
        });

        topAppBar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(mSend, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

        //选择号码列
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("哪列存储着号码？")
                .setItems(DataLoader.getTitles(), (dialog, which) -> {
                    DataLoader.setNumberColumn(DataLoader.getTitles()[which]);
                    ToastUtil.show(this, "号码列: " + DataLoader.getTitles()[which]);
                    dialog.dismiss();
                }).setCancelable(false).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SMSSender.SENT_SMS_ACTION);
        BroadcastReceiver receiver = new BroadcastReceiver() {
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
        };

        registerReceiver(receiver, filter);
        this.receiver = receiver;


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
