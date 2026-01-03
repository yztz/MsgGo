package top.yztz.msggo.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.transition.MaterialSharedAxis;

import top.yztz.msggo.data.DataCleaner;
import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.R;
import top.yztz.msggo.util.ToastUtil;

public class SettingFrag extends Fragment {
    private static final String TAG = "SettingFrag";
    private Context context;
    private MaterialSwitch mSwitchAutoEditor;
    private MaterialCardView mCardClearCache;
    private View mRowExportLog, mRowAboutApp, mRowLanguage;
    private TextView mTvCache, mTvDelayValue, mTvSmsRateValue, mTvLanguage;
    private LinearLayout mCardSmsRate;
    private boolean isUpdatingUI = false;
    private Slider mSliderDelay;

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
        context = getContext();

        mSliderDelay = view.findViewById(R.id.slider_delay);
        mTvDelayValue = view.findViewById(R.id.tv_delay_value);
        mSwitchAutoEditor = view.findViewById(R.id.switch_auto_editor);
        mTvCache = view.findViewById(R.id.tv_cache);
        mCardClearCache = view.findViewById(R.id.card_clear_cache);
        mCardSmsRate = view.findViewById(R.id.card_sms_rate);
        mTvSmsRateValue = view.findViewById(R.id.tv_sms_rate_value);
        mRowExportLog = view.findViewById(R.id.row_export_log);
        mRowAboutApp = view.findViewById(R.id.row_about_app);
        mRowLanguage = view.findViewById(R.id.row_language);
        mTvLanguage = view.findViewById(R.id.tv_language);

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
                mTvDelayValue.setText(String.format(Locale.getDefault(),"%.1fs", seconds));
            }
        });

        mSliderDelay.setLabelFormatter(value -> {
            float seconds = value * 0.5f; // 转换回秒数
            return String.format(Locale.getDefault(), "%.1fs", seconds);
        });

        // SMS Rate
        mCardSmsRate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null);
            EditText editText = dialogView.findViewById(R.id.edit_text);
            editText.setText(DataLoader.getSmsRate());
            editText.setSelection(editText.getText().length());

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.set_sms_rate_title))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String input = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) {
                            DataLoader.setSmsRate("0");
                        } else {
                            try {
                                double rate = Double.parseDouble(input);
                                if (rate >= 0 && rate <= 10) {
                                    DataLoader.setSmsRate(input);
                                } else {
                                    ToastUtil.show(context, getString(R.string.error_invalid_rate_range));
                                }
                            } catch (NumberFormatException e) {
                                ToastUtil.show(context, getString(R.string.error_invalid_number));
                            }
                        }
                        showInfo();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });


        // Clear Cache
        mCardClearCache.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.clear_cache))
                    .setMessage(getString(R.string.confirm_clear_cache_msg))
                    .setPositiveButton(getString(R.string.clear), (dialog, which) -> {
                        DataCleaner.cleanInternalCache(context);
                        HistoryManager.clearHistory(context);
                        DataLoader.clear();
                        ToastUtil.show(context, getString(R.string.cache_cleared));
                        showInfo();
                    })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
        });

        // Export Log
        mRowExportLog.setOnClickListener(v -> exportLogs());

        // About App
        mRowAboutApp.setOnClickListener(v -> {
            startActivity(new Intent(context, top.yztz.msggo.activities.AboutActivity.class));
        });

        // Language
        mRowLanguage.setOnClickListener(v -> {
            String[] langs = {
                    getString(R.string.language_auto),
                    getString(R.string.language_en),
                    getString(R.string.language_zh)
            };
            String[] tags = {"auto", "en", "zh"};
            String current = DataLoader.getLanguage();
            int checkedItem = 0;
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals(current)) {
                    checkedItem = i;
                    break;
                }
            }

            new MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.switch_language))
                    .setSingleChoiceItems(langs, checkedItem, (dialog, which) -> {
                        top.yztz.msggo.util.LocaleUtils.setLocale(tags[which]);
                        dialog.dismiss();
                        showInfo();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });

    }

    private void exportLogs() {
        try {
            int pid = android.os.Process.myPid();
            Process process = Runtime.getRuntime().exec("logcat -d --pid=" + pid);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }

            File logFile = new File(context.getCacheDir(), "msggo_debug_log.txt");
            FileOutputStream fos = new FileOutputStream(logFile);
            fos.write(log.toString().getBytes());
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", logFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(intent, "导出调试日志"));

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error exporting logs", e);
            ToastUtil.show(context, getString(R.string.export_log_failed_prefix, e.getMessage()));
        }
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
        isUpdatingUI = true;
        
        // Display delay
        int delayMs = DataLoader.getDelay();
        float sliderValue = delayMs / 500f; // 每 0.5 秒为 1 个单位
        sliderValue = Math.max(1f, Math.min(16f, sliderValue)); // 限制在 0.5s-8.0s 范围
        mSliderDelay.setValue(sliderValue);
        mTvDelayValue.setText(String.format(Locale.getDefault(),"%.1fs", sliderValue * 500 / 1000));

        // Set auto editor switch
        mSwitchAutoEditor.setChecked(DataLoader.autoEnterEditor());

        // Display number column
        // mTvNumberColumn.setText(TextUtils.isEmpty(numberColumn) ? "未选择" : numberColumn);
        
        // SMS Rate
        mTvSmsRateValue.setText(getString(R.string.currency_sms_rate, DataLoader.getSmsRate()));

        // Display cache size
        try {
            String cacheSize = DataCleaner.getCacheSize(context.getCacheDir());
            mTvCache.setText(getString(R.string.current_cache_size_prefix, cacheSize));
        } catch (Exception e) {
            mTvCache.setText(getString(R.string.error_calc_cache_size));
        }

        // Display language
        String lang = DataLoader.getLanguage();
        String langText = getString(R.string.language_auto);
        if ("en".equals(lang)) langText = getString(R.string.language_en);
        else if ("zh".equals(lang)) langText = getString(R.string.language_zh);
        mTvLanguage.setText(langText);
        
        isUpdatingUI = false;
    }

}
