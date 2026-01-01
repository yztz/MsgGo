package top.yzzblog.messagehelper.activities;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.adapters.ListAdapter;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.data.Message;
import top.yzzblog.messagehelper.services.MessageService;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.services.SendingMonitor;

import top.yzzblog.messagehelper.util.FileUtil;
import top.yzzblog.messagehelper.util.TextParser;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.adapters.CheckboxAdapter;
import android.widget.CheckBox;

public class ChooserActivity extends AppCompatActivity {
    private static final String TAG = "ChooserActivity";
    private RecyclerView mRv;
    private RecyclerView rvCheckbox;
    private Button mSend;
    private CheckBox cbSelectAll;
    private MaterialToolbar topAppBar;
    private TextView tvFileName, tvSimInfo, tvSelectionCount, tvEstimatedCost;
    private LinearLayout layoutHeader;
    private CheckboxAdapter checkboxAdapter;
    
    // Sending Progress UI
    private BottomSheetDialog progressDialog;
    private TextView tvProgressTitle, tvProgressCount, tvLogs;
    private LinearProgressIndicator progressBar;
    private ScrollView scrollLogs;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        mRv = findViewById(R.id.rv_data);
        rvCheckbox = findViewById(R.id.rv_checkbox);
        mSend = findViewById(R.id.btn_send);
        cbSelectAll = findViewById(R.id.cb_select_all);
        topAppBar = findViewById(R.id.topAppBar);
        tvFileName = findViewById(R.id.tv_file_name);
        tvSimInfo = findViewById(R.id.tv_sim_info);
        tvSelectionCount = findViewById(R.id.tv_selection_count);
        tvEstimatedCost = findViewById(R.id.tv_estimated_cost);
        layoutHeader = findViewById(R.id.layout_header);

        final RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRv.setLayoutManager(manager);
        final ListAdapter adapter = new ListAdapter(this);
        mRv.setAdapter(adapter);
        adapter.setOnItemClickListener(position -> {
            String template = DataLoader.getContent();
            Map<String, String> dataMap = DataLoader.getDataModel().getMap(position);
            String content = TextParser.parse(template, dataMap);
            String recipient = dataMap.get(DataLoader.getNumberColumn());

            new MaterialAlertDialogBuilder(this)
                    .setTitle("消息预览")
                    .setMessage(String.format("收件人：%s\n内容预览：\n%s",
                            TextUtils.isEmpty(recipient) ? "未找到号码" : recipient,
                            content))
                    .setPositiveButton("确定", null)
                    .show();
        });

        final RecyclerView.LayoutManager checkboxManager = new LinearLayoutManager(this);
        rvCheckbox.setLayoutManager(checkboxManager);
        checkboxAdapter = new CheckboxAdapter(this);
        rvCheckbox.setAdapter(checkboxAdapter);

        checkboxAdapter.setOnSelectionChangedListener((position, isChecked) -> updateSelectionSummary());

        // Synchronize scrolling
        RecyclerView.OnScrollListener syncScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView == mRv) {
                        rvCheckbox.scrollBy(0, dy);
                    } else if (recyclerView == rvCheckbox) {
                        mRv.scrollBy(0, dy);
                    }
                }
            }
        };
        mRv.addOnScrollListener(syncScrollListener);
        rvCheckbox.addOnScrollListener(syncScrollListener);

        // Select All Logic
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkboxAdapter.setAllCheckBoxChosen(isChecked);
            updateSelectionSummary();
        });

        mSend.setOnClickListener(v -> {
            ArrayList<Integer> itemIndices = new ArrayList<>();
            SparseBooleanArray checkedMap = checkboxAdapter.getCheckedMap();
            int itemCount = mRv.getAdapter() != null ? mRv.getAdapter().getItemCount() : 0;
            for (int i = 0; i < itemCount; i++) {
                if (checkedMap.get(i)) {
                    itemIndices.add(i);
                }
            }

            if (itemIndices.isEmpty()) {
                ToastUtil.show(ChooserActivity.this, "当前还未选择任何收件人哦~");
                return;
            }

            double rate = 0.1;
            try {
                rate = Double.parseDouble(DataLoader.getSmsRate());
            } catch (Exception ignored) {}
            double cost = itemIndices.size() * rate;

            new MaterialAlertDialogBuilder(this)
                    .setTitle("确认发送")
                    .setMessage(String.format(Locale.getDefault(), "确定向已选的 %d 位联系人发送短信吗？\n预计产生费用：¥%.2f", itemIndices.size(), cost))
                    .setPositiveButton("立即发送", (dialog, which) -> startSending(itemIndices))
                    .setNegativeButton("取消", null)
                    .show();
        });



        topAppBar.setNavigationOnClickListener(v -> finish());
//        setupNumberColumnSelection();
        setupInfoCard();
        setupTableHeader();
        updateSelectionSummary();
        
        // Observe Sending Status
        observeSending();
    }

    private void updateSelectionSummary() {
        if (checkboxAdapter == null || tvSelectionCount == null || tvEstimatedCost == null) return;
        
        int total = DataLoader.getDataModel() == null ? 0 : DataLoader.getDataModel().getSize();
        int selected = 0;
        SparseBooleanArray checkedMap = checkboxAdapter.getCheckedMap();
        for (int i = 0; i < total; i++) {
            if (checkedMap.get(i)) {
                selected++;
            }
        }

        double rate = 0.1;
        try {
            rate = Double.parseDouble(DataLoader.getSmsRate());
        } catch (Exception ignored) {}

        double cost = selected * rate;
        tvSelectionCount.setText(String.format(Locale.getDefault(), "%d / %d", selected, total));
        tvEstimatedCost.setText(String.format(Locale.getDefault(), "%.2f", cost));
    }
    
    private void setupInfoCard() {
        String path = DataLoader.getLastPath();
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            tvFileName.setText(file.getName());
        }
        
        int subId = DataLoader.getSimSubId();
        List<SubscriptionInfo> subs = SMSSender.getSubs(this);
        String simName = "未知 SIM 卡";
        if (subs != null) {
            for (SubscriptionInfo sub : subs) {
                if (sub.getSubscriptionId() == subId) {
                    simName = String.format("卡槽 %d · %s", sub.getSimSlotIndex() + 1, sub.getCarrierName());
                    break;
                }
            }
        }
        tvSimInfo.setText(simName);
    }
    
    private void setupTableHeader() {
        String[] titles = DataLoader.getTitles();
        if (titles == null) return;
        
        layoutHeader.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int width = (int) (130 * density); // Match updated layout_data_item width

        // Ensure header background fills at least the screen width - handled by layout weight now
        
        for (String title : titles) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(params);
            tv.setText(title);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 16, 0, 16);
            tv.setMaxLines(1);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            // Use Material 3 text appearance
            tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge);
            layoutHeader.addView(tv);
        }

    }


    
    @Override
    protected void onResume() {
        super.onResume();
        // Restore dialog if sending
        SendingMonitor.SendingState state = SendingMonitor.getInstance().getState().getValue();
        if (state == SendingMonitor.SendingState.SENDING) {
            showProgressDialog();
        }
    }


//    private void setupNumberColumnSelection() {
//        if (!TextUtils.isEmpty(DataLoader.getNumberColumn())) {
//            return;
//        }
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("哪列存储着号码？")
//                .setItems(DataLoader.getTitles(), (dialog, which) -> {
//                    DataLoader.setNumberColumn(DataLoader.getTitles()[which]);
//                    ToastUtil.show(this, "号码列: " + DataLoader.getTitles()[which]);
//                    dialog.dismiss();
//                })
//                .setCancelable(false)
//                .show();
//    }

    private void startSending(ArrayList<Integer> itemIndices) {
        String rawContent = DataLoader.getContent();
        String numberCol = DataLoader.getNumberColumn();

        List<Message> messages = new ArrayList<>();
        for (int i : itemIndices) {
            Map<String, String> tmp = DataLoader.getDataModel().getMap(i);
            String content = TextParser.parse(rawContent, tmp);
            String phoneNumber = tmp.get(numberCol);
            messages.add(new Message(phoneNumber, content));
        }

        String serPath = FileUtil.saveMessageArrayToFile(this, messages.toArray(new Message[0]));
        if (serPath == null) {
            ToastUtil.show(this, "短信服务启动失败");
            return;
        }

        Intent serviceIntent = new Intent(this, MessageService.class);
        serviceIntent.putExtra("delay", DataLoader.getDelay());
        serviceIntent.putExtra("subId", DataLoader.getSimSubId());
        serviceIntent.putExtra("message_file", serPath);

        ContextCompat.startForegroundService(this, serviceIntent);
        showProgressDialog();
    }
    
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new BottomSheetDialog(this);
            progressDialog.setContentView(R.layout.dialog_sending_progress);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            
            tvProgressTitle = progressDialog.findViewById(R.id.tv_progress_title);
            tvProgressCount = progressDialog.findViewById(R.id.tv_progress_count);
            tvLogs = progressDialog.findViewById(R.id.tv_logs);
            progressBar = progressDialog.findViewById(R.id.progress);
            scrollLogs = progressDialog.findViewById(R.id.scroll_logs);
            btnCancel = progressDialog.findViewById(R.id.btn_cancel);
            
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    SendingMonitor.SendingState state = SendingMonitor.getInstance().getState().getValue();
                    if (state == SendingMonitor.SendingState.SENDING) {
                        Intent intent = new Intent(this, MessageService.class);
                        intent.setAction(MessageService.ACTION_CANCEL);
                        startService(intent);
                    } else {
                        progressDialog.dismiss();
                    }
                });
            }
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        
        // Refresh UI state
        refreshDialogUI();
    }
    
    private void refreshDialogUI() {
       updateProgressUI(
               SendingMonitor.getInstance().getProgress().getValue(),
               SendingMonitor.getInstance().getTotal().getValue()
       );
    }
    
    private void updateProgressUI(Integer progress, Integer total) {
        if (progress == null) progress = 0;
        if (total == null) total = 0;
        
        if (progressBar != null) {
            progressBar.setMax(total);
            progressBar.setProgress(progress);
        }
        if (tvProgressCount != null) {
            tvProgressCount.setText(String.format("%d/%d", progress, total));
        }
    }
    
    private void observeSending() {
        SendingMonitor.getInstance().getProgress().observe(this, progress -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                updateProgressUI(progress, SendingMonitor.getInstance().getTotal().getValue());
            }
        });
        
        SendingMonitor.getInstance().getTotal().observe(this, total -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                updateProgressUI(SendingMonitor.getInstance().getProgress().getValue(), total);
            }
        });
        
        SendingMonitor.getInstance().getLogs().observe(this, logs -> {
            if (progressDialog != null && progressDialog.isShowing() && tvLogs != null) {
                tvLogs.setText(logs);
                if (scrollLogs != null) scrollLogs.fullScroll(View.FOCUS_DOWN);
            }
        });
        
        SendingMonitor.getInstance().getState().observe(this, state -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                switch (state) {
                    case COMPLETED:
                        tvProgressTitle.setText("发送完成");
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        btnCancel.setText("完成");
                        break;
                    case CANCELLED:
                        tvProgressTitle.setText("已取消");
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        btnCancel.setText("关闭");
                        break;
                    case SENDING:
                        tvProgressTitle.setText("正在发送...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        btnCancel.setText("取消");
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
