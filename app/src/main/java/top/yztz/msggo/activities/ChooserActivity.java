package top.yztz.msggo.activities;

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

import top.yztz.msggo.R;
import top.yztz.msggo.adapters.ListAdapter;
import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.data.Message;
import top.yztz.msggo.services.MessageService;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.services.SendingMonitor;

import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.TextParser;
import top.yztz.msggo.util.ToastUtil;
import top.yztz.msggo.adapters.CheckboxAdapter;
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
    private TextView tvProgressTitle, tvSentCount, tvConfirmedCount, tvLogs;
    private LinearProgressIndicator progressSent, progressConfirmed;
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
                    .setTitle(getString(R.string.preview_title))
                    .setMessage(getString(R.string.preview_msg_format,
                            TextUtils.isEmpty(recipient) ? getString(R.string.unknown) : recipient,
                            content))
                    .setPositiveButton(getString(R.string.ok), null)
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
                ToastUtil.show(ChooserActivity.this, getString(R.string.no_recipients_selected));
                return;
            }

            double rate = 0.1;
            try {
                rate = Double.parseDouble(DataLoader.getSmsRate());
            } catch (Exception ignored) {}
            double cost = itemIndices.size() * rate;

            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.confirm_send_title))
                    .setMessage(getString(R.string.confirm_send_msg_format, itemIndices.size(), cost))
                    .setPositiveButton(getString(R.string.send_now), (dialog, which) -> startSending(itemIndices))
                    .setNegativeButton(getString(R.string.cancel), null)
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
        String simName = getString(R.string.unknown_sim);
        if (subs != null) {
            for (SubscriptionInfo sub : subs) {
                if (sub.getSubscriptionId() == subId) {
                    simName = getString(R.string.sim_slot_format, sub.getSimSlotIndex() + 1, sub.getCarrierName());
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

        MessageService.startSending(this, messages, DataLoader.getSimSubId(), DataLoader.getDelay());
        showProgressDialog();
    }
    
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new BottomSheetDialog(this);
            progressDialog.setContentView(R.layout.dialog_sending_progress);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            
            tvProgressTitle = progressDialog.findViewById(R.id.tv_progress_title);
            tvSentCount = progressDialog.findViewById(R.id.tv_sent_count);
            tvConfirmedCount = progressDialog.findViewById(R.id.tv_confirmed_count);
            tvLogs = progressDialog.findViewById(R.id.tv_logs);
            progressSent = progressDialog.findViewById(R.id.progress_sent);
            progressConfirmed = progressDialog.findViewById(R.id.progress_confirmed);
            scrollLogs = progressDialog.findViewById(R.id.scroll_logs);
            btnCancel = progressDialog.findViewById(R.id.btn_cancel);
            
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    SendingMonitor.SendingState state = SendingMonitor.getInstance().getState().getValue();
                    if (state == SendingMonitor.SendingState.SENDING) {
                        MessageService.stopSending(ChooserActivity.this);
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
        Integer total = SendingMonitor.getInstance().getTotal().getValue();
        updateSentProgressUI(SendingMonitor.getInstance().getSentProgress().getValue(), total);
        updateConfirmedProgressUI(SendingMonitor.getInstance().getConfirmedProgress().getValue(), total);
    }
    
    private void updateSentProgressUI(Integer sent, Integer total) {
        if (sent == null) sent = 0;
        if (total == null) total = 0;
        
        if (progressSent != null) {
            progressSent.setMax(total);
            progressSent.setProgress(sent);
        }
        if (tvSentCount != null) {
            tvSentCount.setText(String.format("%d/%d", sent, total));
        }
    }
    
    private void updateConfirmedProgressUI(Integer confirmed, Integer total) {
        if (confirmed == null) confirmed = 0;
        if (total == null) total = 0;
        
        if (progressConfirmed != null) {
            progressConfirmed.setMax(total);
            progressConfirmed.setProgress(confirmed);
        }
        if (tvConfirmedCount != null) {
            tvConfirmedCount.setText(String.format("%d/%d", confirmed, total));
        }
    }
    
    private void observeSending() {
        SendingMonitor.getInstance().getSentProgress().observe(this, sent -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                updateSentProgressUI(sent, SendingMonitor.getInstance().getTotal().getValue());
            }
        });
        
        SendingMonitor.getInstance().getConfirmedProgress().observe(this, confirmed -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                updateConfirmedProgressUI(confirmed, SendingMonitor.getInstance().getTotal().getValue());
            }
        });
        
        SendingMonitor.getInstance().getTotal().observe(this, total -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                updateSentProgressUI(SendingMonitor.getInstance().getSentProgress().getValue(), total);
                updateConfirmedProgressUI(SendingMonitor.getInstance().getConfirmedProgress().getValue(), total);
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
                        tvProgressTitle.setText(getString(R.string.sending_completed));
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        btnCancel.setText(getString(R.string.done));
                        break;
                    case CANCELLED:
                        tvProgressTitle.setText(getString(R.string.cancelled));
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        btnCancel.setText(getString(R.string.close));
                        break;
                    case SENDING:
                        tvProgressTitle.setText(getString(R.string.sending));
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        btnCancel.setText(getString(R.string.cancel));
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
