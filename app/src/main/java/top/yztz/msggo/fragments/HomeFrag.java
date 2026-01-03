package top.yztz.msggo.fragments;

import static top.yztz.msggo.util.XiaomiUtil.showXiaomiPermissionDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.yztz.msggo.activities.EditActivity;
import top.yztz.msggo.activities.MainActivity;
import top.yztz.msggo.data.DataContext;
import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.R;
import top.yztz.msggo.services.LoadService;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.ToastUtil;
import top.yztz.msggo.util.XiaomiUtil;
import top.yztz.msggo.activities.ChooserActivity;

public class HomeFrag extends Fragment {
    private static final String TAG = "HomeFrag";
    private Context context;
    
    private View cardCurrentFile, cardNumberColumn, rowEditContent, rowSelectSim, rowSend;
    private TextView tvSimInfo, tvSubtitleSend, tvSubtitleEdit, tvEmptyHistory;
    private TextView tvCurrentFilePath, tvCurrentNumberColumn;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    
    private List<SubscriptionInfo> subs;
    private int simSubId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_home, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvSimInfo = view.findViewById(R.id.tv_sim_info);
        tvSubtitleSend = view.findViewById(R.id.tv_subtitle_send);
        tvSubtitleEdit = view.findViewById(R.id.tv_subtitle_edit);
        rvHistory = view.findViewById(R.id.rv_history);
        tvEmptyHistory = view.findViewById(R.id.tv_empty_history);
        
        cardCurrentFile = view.findViewById(R.id.card_current_file);
        cardNumberColumn = view.findViewById(R.id.card_number_column);
        rowEditContent = view.findViewById(R.id.row_edit_content);
        rowSelectSim = view.findViewById(R.id.row_select_sim);
        rowSend = view.findViewById(R.id.row_send);
        tvCurrentFilePath = view.findViewById(R.id.tv_current_file_path);
        tvCurrentNumberColumn = view.findViewById(R.id.tv_current_number_column);

        setupHistoryList();
        
        simSubId = DataLoader.getSimSubId();
        
        setupClickListeners();
        loadSimInfo();
        updateStatus();
    }

    private void setupClickListeners() {
        // Send button
        rowSend.setOnClickListener(v -> {
            if (DataLoader.getDataModel() == null) {
                ToastUtil.show(context, getString(R.string.error_load_data_first));
            } else if (TextUtils.isEmpty(DataLoader.getContent())) {
                ToastUtil.show(context, getString(R.string.error_edit_content_first));
            } else {
                startActivity(new Intent(context, ChooserActivity.class));
            }
        });
        
        // Content editing
        rowEditContent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditActivity.class);
            startActivity(intent);
        });
        
        // SIM selection
        rowSelectSim.setOnClickListener(v -> showSimSelector());

        // File import
        cardCurrentFile.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFileChooser();
            }
        });

        // Number column selection
        cardNumberColumn.setOnClickListener(v -> showNumberColumnSelector());
    }

    private void loadSimInfo() {
        subs = SMSSender.getSubs(getContext());
        if (subs.isEmpty() && XiaomiUtil.isXiaomi()) {
            showXiaomiPermissionDialog(getActivity());
            return;
        }
        updateSimDisplay();
    }

    private void showSimSelector() {
        if (subs == null || subs.isEmpty()) {
            ToastUtil.show(context, getString(R.string.error_no_sim));
            return;
        }

        String[] options = new String[subs.size()];
        int selected = 0;
        
        for (int i = 0; i < subs.size(); i++) {
            SubscriptionInfo sub = subs.get(i);
            options[i] = getString(R.string.slot_description, sub.getSimSlotIndex() + 1, sub.getCarrierName());
            if (sub.getSubscriptionId() == simSubId) {
                selected = i;
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.select_sim_dialog_title))
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    simSubId = subs.get(which).getSubscriptionId();
                    DataLoader.setSimSubId(simSubId);
                    updateSimDisplay();
                    ToastUtil.show(context, getString(R.string.selected_prefix, options[which]));
                    dialog.dismiss();
                })
                .show();
    }

    private void updateSimDisplay() {
        if (subs == null || subs.isEmpty()) {
            tvSimInfo.setText(getString(R.string.no_available_sim));
            return;
        }

        for (SubscriptionInfo sub : subs) {
            if (sub.getSubscriptionId() == simSubId) {
                String carrierName = sub.getCarrierName().toString();
                tvSimInfo.setText(getString(R.string.slot_description, sub.getSimSlotIndex() + 1, carrierName));
                return;
            }
        }
        
        // Default to first SIM if saved one not found
        simSubId = subs.get(0).getSubscriptionId();
        DataLoader.setSimSubId(simSubId);
        SubscriptionInfo first = subs.get(0);
        tvSimInfo.setText(getString(R.string.slot_description, first.getSimSlotIndex() + 1, first.getCarrierName()));
    }

    private void showNumberColumnSelector() {
        String[] titles = DataLoader.getTitles();
        if (titles == null || titles.length == 0) {
            ToastUtil.show(context, getString(R.string.error_load_data_first));
            return;
        }

        int checkedItem = -1;
        String currentColumn = DataLoader.getNumberColumn();
        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equals(currentColumn)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("选择号码列")
                .setSingleChoiceItems(titles, checkedItem, (dialog, which) -> {
                    Log.i(TAG, "选择号码列: " + titles[which]);
                    DataLoader.setNumberColumn(titles[which]);
                    HistoryManager.addHistory(context, DataLoader.getDataContext());
                    updateStatus();
                    dialog.dismiss();
                })
                .show();
    }

    public void updateStatus() {
        // Progressive Disclosure State
        boolean hasData = false;
        boolean hasContent = false;
        boolean hasSim = false;

        // 1. Data Status
        String path = DataLoader.getLastPath();
        if (DataLoader.getDataModel() != null && !TextUtils.isEmpty(path)) {
            hasData = true;
            int count = DataLoader.getDataModel().getSize();
            tvSubtitleSend.setText(getString(R.string.data_ready_format, count));
            
            String fileName = path;
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0) fileName = path.substring(lastSlash + 1);
            tvCurrentFilePath.setText(fileName);
        } else {
            tvSubtitleSend.setText(getString(R.string.no_data_imported));
            tvCurrentFilePath.setText(getString(R.string.click_to_import));
        }

        // 2. Content Status
        String content = DataLoader.getContent();
        if (!TextUtils.isEmpty(content)) {
            hasContent = true;
            tvSubtitleEdit.setText(getString(R.string.template_ready));
        } else {
            tvSubtitleEdit.setText(getString(R.string.no_template));
        }

        // 3. SIM Status
        // SIM Info is updated in updateSimDisplay() which is called on creation and when SIM changes.
        // We consider SIM "set" if simSubId is valid.
        if (simSubId != -1) {
            hasSim = true;
        }

        // Progressive Visibility Logic
        // Always show Data row
        cardCurrentFile.setVisibility(View.VISIBLE);
        
        // Show Column & Content rows only after Data is loaded
        if (hasData) {
            cardNumberColumn.setVisibility(View.VISIBLE);
            rowEditContent.setVisibility(View.VISIBLE);
        } else {
            cardNumberColumn.setVisibility(View.GONE);
            rowEditContent.setVisibility(View.GONE);
        }
        
        // Show SIM row only after Content is set
        if (hasData && hasContent) {
            rowSelectSim.setVisibility(View.VISIBLE);
        } else {
            rowSelectSim.setVisibility(View.GONE);
        }
        
        // Show Send row only after SIM information is retrieved (or just always after SIM row is shown if SIM is usually pre-available)
        // Let's require SIM and Data and Content for Send row to appear.
        if (hasData && hasContent && hasSim && subs != null && !subs.isEmpty()) {
            rowSend.setVisibility(View.VISIBLE);
        } else {
            rowSend.setVisibility(View.GONE);
        }

        // Update Number Column Text
        String numberColumn = DataLoader.getNumberColumn();
        tvCurrentNumberColumn.setText(TextUtils.isEmpty(numberColumn) ? "未选择" : numberColumn);

        loadHistory();
    }

    private void setupHistoryList() {
        rvHistory.setLayoutManager(new LinearLayoutManager(context));
        historyAdapter = new HistoryAdapter();
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadHistory() {
        if (historyAdapter != null) {
            List<DataContext> history = HistoryManager.getHistory(context);
            historyAdapter.setItems(history);
            
            if (history.isEmpty()) {
                rvHistory.setVisibility(View.GONE);
                tvEmptyHistory.setVisibility(View.VISIBLE);
            } else {
                rvHistory.setVisibility(View.VISIBLE);
                tvEmptyHistory.setVisibility(View.GONE);
            }
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<DataContext> items = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        public void setItems(List<DataContext> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataContext item = items.get(position);
            holder.tvFileName.setText(item.getFileName());
            holder.tvDate.setText(dateFormat.format(new Date(item.timestamp)));
            
            String template = item.template;
            if (TextUtils.isEmpty(template)) {
                holder.tvTemplatePreview.setText(getString(R.string.no_template_content));
            } else {
                holder.tvTemplatePreview.setText(template.replace("\n", " "));
            }

            holder.itemView.setOnClickListener(v -> {
                DataLoader.load(item.path, context);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFileName, tvTemplatePreview, tvDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvFileName = itemView.findViewById(R.id.tv_file_name);
                tvTemplatePreview = itemView.findViewById(R.id.tv_template_preview);
                tvDate = itemView.findViewById(R.id.tv_date);
            }
        }
    }
}
