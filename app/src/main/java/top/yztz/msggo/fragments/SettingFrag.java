/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package top.yztz.msggo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialSharedAxis;

import top.yztz.msggo.data.DataCleaner;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.R;
import top.yztz.msggo.data.SettingManager;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.util.LocaleUtils;
import top.yztz.msggo.util.ToastUtil;

public class SettingFrag extends Fragment {
    private static final String TAG = "SettingFrag";
    private Context context;
    private MaterialSwitch mSwitchAutoEditor, mSwitchRandomizeDelay;
    private MaterialCardView mCardClearCache;
    private View mRowExportLog, mRowAboutApp, mRowLanguage, mRowCheckUpdate;
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

        mTvDelayValue = view.findViewById(R.id.tv_delay_value);
        mSwitchAutoEditor = view.findViewById(R.id.switch_auto_editor);
        mSwitchRandomizeDelay = view.findViewById(R.id.switch_randomize_delay);
        mTvCache = view.findViewById(R.id.tv_cache);
        mCardClearCache = view.findViewById(R.id.card_clear_cache);
        mCardSmsRate = view.findViewById(R.id.card_sms_rate);
        mTvSmsRateValue = view.findViewById(R.id.tv_sms_rate_value);
        mRowExportLog = view.findViewById(R.id.row_export_log);
        mRowAboutApp = view.findViewById(R.id.row_about_app);
        mRowLanguage = view.findViewById(R.id.row_language);
        mTvLanguage = view.findViewById(R.id.tv_language);
        mRowCheckUpdate = view.findViewById(R.id.row_check_update);

        mSliderDelay = view.findViewById(R.id.slider_delay);
        mSliderDelay.setValueFrom(Settings.SEND_DELAY_MIN);
        mSliderDelay.setValueTo(Settings.SEND_DELAY_MAX);
        mSliderDelay.setStepSize(Settings.SEND_DELAY_STEP_UNIT);

        setupListeners();
        showInfo();
    }

    private void setupListeners() {
        // Auto-save: Auto Editor Switch
        mSwitchAutoEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) {
                SettingManager.setAutoEnterEditor(isChecked);
            }
        });

        mSwitchRandomizeDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) {
                SettingManager.setRandomizeDelay(isChecked);
            }
        });

        // Auto-save: Slider Delay
        mSliderDelay.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                SettingManager.setDelay((int)value);
                float seconds = value / 1000f;
                mTvDelayValue.setText(String.format(Locale.getDefault(),"%.1fs", seconds));
            }
        });

        mSliderDelay.setLabelFormatter(value -> {
            float seconds = value / 1000f; // 转换回秒数
            return String.format(Locale.getDefault(), "%.1fs", seconds);
        });

        // SMS Rate
        mCardSmsRate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null);
            TextInputLayout container = dialogView.findViewById(R.id.edit_text_container);
            container.setHint(R.string.hint_sms_rate);
            container.setPrefixText(getString(R.string.currency_unit));

            EditText editText = dialogView.findViewById(R.id.edit_text);
            editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_CLASS_NUMBER);
            editText.setText(String.format(Locale.getDefault(), "%f", SettingManager.getSmsRate()));
            editText.setSelection(editText.getText().length());

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.set_sms_rate_title))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String input = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) {
                            SettingManager.setSmsRate(0.0f);
                        } else {
                            try {
                                float rate = Float.parseFloat(input);
                                if (rate >= Settings.SMS_RATE_MIN && rate <= Settings.SMS_RATE_MAX) {
                                    SettingManager.setSmsRate(rate);
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
        mCardClearCache.setOnClickListener(v -> new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.clear_cache))
                .setMessage(getString(R.string.confirm_clear_cache_msg))
                .setPositiveButton(getString(R.string.clear), (dialog, which) -> {
                    DataCleaner.cleanInternalCache(context);
                    HistoryManager.clearHistory(context);
                    DataModel.clear();
                    ToastUtil.show(context, getString(R.string.cache_cleared));
                    showInfo();
                })
            .setNegativeButton(getString(R.string.cancel), null)
            .show());

        // Export Log
        mRowExportLog.setOnClickListener(v -> exportLogs());

        // About App
        mRowAboutApp.setOnClickListener(v -> startActivity(new Intent(context, top.yztz.msggo.activities.AboutActivity.class)));

        // Language
        mRowLanguage.setOnClickListener(v -> {
            String current = SettingManager.getLanguage();
            String[] tags = LocaleUtils.getSupportedLanguages(context);
            String[] langs = new String[tags.length];
            int checkedItem = 0;
            for (int i = 0; i < tags.length; i++) {
                langs[i] = LocaleUtils.getLanguageDisplayName(context, tags[i]);
                if (tags[i].equals(current)) {
                    checkedItem = i;
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

        // Check Update
        mRowCheckUpdate.setOnClickListener(v -> {
            String releaseUrl = "https://github.com/yztz/MsgGo/releases";
            new MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.check_update))
                    .setMessage(getString(R.string.going_to_url, releaseUrl))
                    .setPositiveButton(getString(R.string.visit), (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl));
                            startActivity(intent);
                        } catch (Exception e) {
                            ToastUtil.show(context, getString(R.string.cannot_open_link, e.getMessage()));
                        }
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
        float delay = SettingManager.getDelay();
        mSliderDelay.setValue(delay);
        mTvDelayValue.setText(String.format(Locale.getDefault(),"%.1fs", delay/1000f));

        // Set switches
        mSwitchAutoEditor.setChecked(SettingManager.autoEnterEditor());
        mSwitchRandomizeDelay.setChecked(SettingManager.isRandomizeDelay());

        // Display number column
        // mTvNumberColumn.setText(TextUtils.isEmpty(numberColumn) ? "未选择" : numberColumn);
        
        // SMS Rate
        mTvSmsRateValue.setText(getString(R.string.currency_sms_rate, SettingManager.getSmsRate()));

        // Display cache size
        try {
            String cacheSize = DataCleaner.getCacheSize(context.getCacheDir());
            mTvCache.setText(getString(R.string.current_cache_size_prefix, cacheSize));
        } catch (Exception e) {
            mTvCache.setText(getString(R.string.error_calc_cache_size));
        }

        // Display language
        String langText = LocaleUtils.getLanguageDisplayName(context, SettingManager.getLanguage());
        mTvLanguage.setText(langText);
        
        isUpdatingUI = false;
    }

}
