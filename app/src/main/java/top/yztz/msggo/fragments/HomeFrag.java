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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.transition.Fade;
import androidx.transition.ChangeBounds;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import top.yztz.msggo.R;
import top.yztz.msggo.activities.ChooserActivity;
import top.yztz.msggo.activities.EditActivity;
import top.yztz.msggo.activities.MainActivity;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.services.LoadService;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.ToastUtil;
import top.yztz.msggo.util.XiaomiUtil;

public class HomeFrag extends Fragment {
    private static final String TAG = "HomeFrag";
    private Context context;

    private View rowCurrentFile, rowNumberColumn, rowEditContent, rowSelectSim, rowSend, cardSend;
    private ViewGroup containerHome;
    private TextView tvSimInfo, tvSubtitleSend, tvSubtitleEdit, tvEmptyHistory;
    private TextView tvCurrentFilePath, tvCurrentNumberColumn;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    private List<SubscriptionInfo> subs;
//    private int simSubId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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

        rowCurrentFile = view.findViewById(R.id.row_current_file);
        rowNumberColumn = view.findViewById(R.id.row_number_column);
        rowEditContent = view.findViewById(R.id.row_edit_content);
        rowSelectSim = view.findViewById(R.id.row_select_sim);
        rowSend = view.findViewById(R.id.row_send);
        cardSend = view.findViewById(R.id.card_send);
        containerHome = view.findViewById(R.id.container_home);
        tvCurrentFilePath = view.findViewById(R.id.tv_current_file_path);
        tvCurrentNumberColumn = view.findViewById(R.id.tv_current_number_column);

        setupHistoryList();

//        simSubId = DataModel.getSubId();

        setupClickListeners();
        loadSimInfo();
        updateStatus();
    }

    private void setupClickListeners() {
        // Send button
        rowSend.setOnClickListener(v -> {
            if (!DataModel.loaded()) {
                ToastUtil.show(context, getString(R.string.error_load_data_first));
            } else if (TextUtils.isEmpty(DataModel.getTemplate())) {
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
        rowCurrentFile.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFileChooser();
            }
        });

        // Number column selection
        rowNumberColumn.setOnClickListener(v -> showNumberColumnSelector());
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
            if (sub.getSubscriptionId() == DataModel.getSubId()) {
                selected = i;
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.select_sim_dialog_title))
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    int simSubId = subs.get(which).getSubscriptionId();
                    DataModel.setSubId(simSubId);
                    DataModel.saveAsHistory(getContext());
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
            if (sub.getSubscriptionId() == DataModel.getSubId()) {
                String carrierName = sub.getCarrierName().toString();
                tvSimInfo.setText(getString(R.string.slot_description, sub.getSimSlotIndex() + 1, carrierName));
                return;
            }
        }

        // Default to first SIM if saved one not found
        int simSubId = subs.get(0).getSubscriptionId();
        DataModel.setSubId(simSubId);
        SubscriptionInfo first = subs.get(0);
        tvSimInfo.setText(getString(R.string.slot_description, first.getSimSlotIndex() + 1, first.getCarrierName()));
    }

    private void showNumberColumnSelector() {
        String[] titles = DataModel.getTitles();
        if (titles == null || titles.length == 0) {
            ToastUtil.show(context, getString(R.string.error_load_data_first));
            return;
        }

        int checkedItem = -1;
        String currentColumn = DataModel.getNumberColumn();
        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equals(currentColumn)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.select_number_column_dialog_title))
                .setSingleChoiceItems(titles, checkedItem, (dialog, which) -> {
                    Log.i(TAG, "选择号码列: " + titles[which]);
                    DataModel.setNumberColumn(titles[which]);
                    DataModel.saveAsHistory(context);
                    updateStatus();
                    dialog.dismiss();
                })
                .show();
    }

    public void updateStatus() {
        if (!isAdded()) return;
        // Progressive Disclosure State
        boolean hasData = false;
        boolean hasContent = false;
//        boolean hasSim = false;

        // 3. SIM Status
        // SIM Info is updated in updateSimDisplay() which is called on creation and
        // when SIM changes.
        // We consider SIM "set" if simSubId is valid.
//        if (DataModel.getSubId() != -1) {
//            hasSim = true;
//        }
        assert DataModel.getSubId() != -1;
        assert subs != null;
        assert !subs.isEmpty();

        TransitionSet transitionSet = new TransitionSet()
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .setDuration(150)
                .setInterpolator(new FastOutSlowInInterpolator());
        TransitionManager.beginDelayedTransition(containerHome, transitionSet);

        // Always show Data row
        rowCurrentFile.setVisibility(View.VISIBLE);

        // 1. Data Status
        if (DataModel.loaded() && !TextUtils.isEmpty(DataModel.getPath())) {
            hasData = true;
            String fileName = DataModel.getPath();
            tvCurrentFilePath.setText(FileUtil.getBriefFilename(fileName));
        } else {
            tvCurrentFilePath.setText(getString(R.string.click_to_import));
        }
        // Show Column & Content rows only after Data is loaded
        if (hasData) {
            rowNumberColumn.setVisibility(View.VISIBLE);
            rowEditContent.setVisibility(View.VISIBLE);

            String numberColumn = DataModel.getNumberColumn();
            tvCurrentNumberColumn.setText(TextUtils.isEmpty(numberColumn) ? "未选择" : numberColumn);
//            rowSelectSim.setVisibility(View.VISIBLE);
        } else {
            rowNumberColumn.setVisibility(View.GONE);
            rowEditContent.setVisibility(View.GONE);
            rowSelectSim.setVisibility(View.GONE);
            cardSend.setVisibility(View.GONE);
        }

        // 2. Content Status
        String template = DataModel.getTemplate();
        if (!TextUtils.isEmpty(template)) {
            hasContent = true;
            tvSubtitleEdit.setText(template.replace("\n", " "));
        } else {
            tvSubtitleEdit.setText(getString(R.string.no_template));
        }

        if (hasData && hasContent) {
            rowSelectSim.setVisibility(View.VISIBLE);
            cardSend.setVisibility(View.VISIBLE);
            updateSimDisplay();
            tvSubtitleSend.setText(getString(R.string.data_ready_format, DataModel.getRowCount()));
        } else {
            cardSend.setVisibility(View.GONE);
            rowSelectSim.setVisibility(View.GONE);
        }

        loadHistory();
    }

    private void setupHistoryList() {
        rvHistory.setLayoutManager(new LinearLayoutManager(context));
        historyAdapter = new HistoryAdapter();
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadHistory() {
        if (historyAdapter != null) {
            List<HistoryManager.HistoryItem> history = HistoryManager.getHistory(context);
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
        private List<HistoryManager.HistoryItem> items = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        public void setItems(List<HistoryManager.HistoryItem> items) {
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
            HistoryManager.HistoryItem item = items.get(position);
            holder.tvFileName.setText(FileUtil.getBriefFilename(item.path));
            holder.tvDate.setText(dateFormat.format(new Date(item.timestamp)));

            String template = item.template;
            if (TextUtils.isEmpty(template)) {
                holder.tvTemplatePreview.setText(getString(R.string.no_template_content));
            } else {
                holder.tvTemplatePreview.setText(template.replace("\n", " "));
            }

            holder.itemView.setOnClickListener(v -> {
                Log.i(TAG, "从历史记录加载：" + item.path);
                LoadService.load(context, item.path);
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
