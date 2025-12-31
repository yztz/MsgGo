package top.yzzblog.messagehelper.fragments;

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

import java.util.ArrayList;
import java.util.List;

import top.yzzblog.messagehelper.activities.EditActivity;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.activities.ChooserActivity;

public class HomeFrag extends Fragment {
    private static final String TAG = "HomeFrag";
    private Context context;
    
    private MaterialCardView cardSend, cardEdit, cardSim;
    private TextView tvSimInfo, tvDataStatus, tvTemplateStatus, tvSimStatus;
    
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
        cardSend = view.findViewById(R.id.card_send);
        cardEdit = view.findViewById(R.id.card_edit);
        cardSim = view.findViewById(R.id.card_sim);
        tvSimInfo = view.findViewById(R.id.tv_sim_info);
        tvDataStatus = view.findViewById(R.id.tv_data_status);
        tvTemplateStatus = view.findViewById(R.id.tv_template_status);
        tvSimStatus = view.findViewById(R.id.tv_sim_status);
        
        simSubId = DataLoader.getSimSubId();
        
        setupClickListeners();
        loadSimInfo();
        updateStatus();
    }

    private void setupClickListeners() {
        // Send button
        cardSend.setOnClickListener(v -> {
            if (DataLoader.getDataModel() == null) {
                ToastUtil.show(context, "请先点击底部 + 导入数据");
            } else if (TextUtils.isEmpty(DataLoader.getContent())) {
                ToastUtil.show(context, "请先编辑短信内容");
            } else {
                startActivity(new Intent(context, ChooserActivity.class));
            }
        });
        
        // Edit button
        cardEdit.setOnClickListener(v -> EditActivity.openEditor(context));
        
        // SIM selection
        cardSim.setOnClickListener(v -> showSimSelector());
    }

    private void loadSimInfo() {
        subs = SMSSender.getSubs(getContext());
        updateSimDisplay();
    }

    private void showSimSelector() {
        if (subs == null || subs.isEmpty()) {
            ToastUtil.show(context, "未检测到可用的 SIM 卡");
            return;
        }

        String[] options = new String[subs.size()];
        int selected = 0;
        
        for (int i = 0; i < subs.size(); i++) {
            SubscriptionInfo sub = subs.get(i);
            options[i] = String.format("卡槽 %d · %s", sub.getSimSlotIndex() + 1, sub.getCarrierName());
            if (sub.getSubscriptionId() == simSubId) {
                selected = i;
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("选择发送 SIM 卡")
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    simSubId = subs.get(which).getSubscriptionId();
                    DataLoader.setSimSubId(simSubId);
                    updateSimDisplay();
                    ToastUtil.show(context, "已选择: " + options[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateSimDisplay() {
        if (subs == null || subs.isEmpty()) {
            tvSimInfo.setText("无可用 SIM");
            tvSimStatus.setText("未检测到");
            return;
        }

        for (SubscriptionInfo sub : subs) {
            if (sub.getSubscriptionId() == simSubId) {
                String carrierName = sub.getCarrierName().toString();
                tvSimInfo.setText(String.format("卡槽 %d", sub.getSimSlotIndex() + 1));
                tvSimStatus.setText(carrierName.length() > 6 ? carrierName.substring(0, 6) + "…" : carrierName);
                return;
            }
        }
        
        // Default to first SIM if saved one not found
        simSubId = subs.get(0).getSubscriptionId();
        DataLoader.setSimSubId(simSubId);
        SubscriptionInfo first = subs.get(0);
        tvSimInfo.setText(String.format("卡槽 %d", first.getSimSlotIndex() + 1));
        tvSimStatus.setText(first.getCarrierName());
    }

    private void updateStatus() {
        // Data status
        if (DataLoader.getDataModel() != null) {
            int count = DataLoader.getDataModel().getSize();
            tvDataStatus.setText(count + " 条");
        } else {
            tvDataStatus.setText("未导入");
        }
        
        // Template status
        String content = DataLoader.getContent();
        if (!TextUtils.isEmpty(content)) {
            int len = content.length();
            tvTemplateStatus.setText(len + " 字");
        } else {
            tvTemplateStatus.setText("未编辑");
        }
        
        // SIM status is updated in updateSimDisplay()
    }
}
