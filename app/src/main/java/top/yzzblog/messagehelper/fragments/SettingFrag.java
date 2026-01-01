package top.yzzblog.messagehelper.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.transition.MaterialSharedAxis;

import top.yzzblog.messagehelper.data.DataCleaner;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.data.HistoryManager;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.services.LoadService;
import top.yzzblog.messagehelper.util.ToastUtil;

public class SettingFrag extends Fragment {
    private static final String TAG = "SettingFrag";
    private Context context;
    private TextView mTvCache, mTvDelayValue, mTvSmsRateValue;
    private Slider mSliderDelay;
    private MaterialSwitch mSwitchAutoEditor;
    private MaterialCardView mCardClearCache;
    private LinearLayout mCardSmsRate;
    private boolean isUpdatingUI = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSliderDelay = view.findViewById(R.id.slider_delay);
        mTvDelayValue = view.findViewById(R.id.tv_delay_value);
        mSwitchAutoEditor = view.findViewById(R.id.switch_auto_editor);
        mTvCache = view.findViewById(R.id.tv_cache);
        mCardClearCache = view.findViewById(R.id.card_clear_cache);
        mCardSmsRate = view.findViewById(R.id.card_sms_rate);
        mTvSmsRateValue = view.findViewById(R.id.tv_sms_rate_value);

        setupListeners();
        showInfo();
    }

    private void setupListeners() {
        // Auto-save: Auto Editor Switch
        mSwitchAutoEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) {
                DataLoader.setAutoEnterEditor(isChecked);
            }
        });

        // Auto-save: Slider Delay
        mSliderDelay.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int delayMs = (int) (value * 500); // 转换回毫秒
                DataLoader.setDelay(delayMs);
                float seconds = value * 0.5f;
                mTvDelayValue.setText(String.format("%.1fs", seconds));
            }
        });

        mSliderDelay.setLabelFormatter(value -> {
            float seconds = value * 0.5f; // 转换回秒数
            return String.format("%.1fs", seconds);
        });

        // SMS Rate
        mCardSmsRate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null);
            EditText editText = dialogView.findViewById(R.id.edit_text);
            editText.setText(DataLoader.getSmsRate());
            editText.setSelection(editText.getText().length());

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle("设置短信资费")
                    .setView(dialogView)
                    .setPositiveButton("确定", (dialog, which) -> {
                        String input = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) {
                            DataLoader.setSmsRate("0");
                        } else {
                            try {
                                double rate = Double.parseDouble(input);
                                if (rate >= 0 && rate <= 10) {
                                    DataLoader.setSmsRate(input);
                                } else {
                                    ToastUtil.show(context, "请输入 0-10 之间的数字");
                                }
                            } catch (NumberFormatException e) {
                                ToastUtil.show(context, "请输入有效的数字");
                            }
                        }
                        showInfo();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });


        // Clear Cache
        mCardClearCache.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle("清除缓存")
                    .setMessage("确定要清空应用缓存和历史记录吗？")
                    .setPositiveButton("清空", (dialog, which) -> {
                        DataCleaner.cleanInternalCache(context);
                        HistoryManager.clearHistory(context);
                        DataLoader.clear();
                        ToastUtil.show(context, "缓存已清空");
                        showInfo();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showInfo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showInfo();
    }

    public void showInfo() {
        if (!isAdded()) return;
        isUpdatingUI = true;
        
        // Display delay
        int delayMs = DataLoader.getDelay();
        float sliderValue = delayMs / 500f; // 每 0.5 秒为 1 个单位
        sliderValue = Math.max(1f, Math.min(16f, sliderValue)); // 限制在 0.5s-8.0s 范围
        mSliderDelay.setValue(sliderValue);
        mTvDelayValue.setText(String.format("%.1fs", sliderValue * 500 / 1000));

        // Set auto editor switch
        mSwitchAutoEditor.setChecked(DataLoader.autoEnterEditor());

        // Display number column
        // mTvNumberColumn.setText(TextUtils.isEmpty(numberColumn) ? "未选择" : numberColumn);
        
        // SMS Rate
        mTvSmsRateValue.setText("￥" + DataLoader.getSmsRate());

        // Display cache size
        try {
            String cacheSize = DataCleaner.getCacheSize(context.getCacheDir());
            mTvCache.setText("当前缓存: " + cacheSize);
        } catch (Exception e) {
            mTvCache.setText("无法计算缓存大小");
        }
        
        isUpdatingUI = false;
    }

}
