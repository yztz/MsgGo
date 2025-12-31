package top.yzzblog.messagehelper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.MaterialSharedAxis;

import top.yzzblog.messagehelper.data.DataCleaner;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.util.ToastUtil;

public class SettingFrag extends Fragment {
    private Context context;
    private TextView mTvPath, mTvCache;
    private TextInputEditText mEtDelay;
    private MaterialSwitch mSwitchAutoEditor;
    private MaterialCardView mCardClearCache;
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

        mTvPath = view.findViewById(R.id.tv_path);
        mEtDelay = view.findViewById(R.id.et_delay);
        mSwitchAutoEditor = view.findViewById(R.id.switch_auto_editor);
        mTvCache = view.findViewById(R.id.tv_cache);
        mCardClearCache = view.findViewById(R.id.card_clear_cache);

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

        // Auto-save: Delay Input with debounce
        mEtDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingUI) return;
                
                String text = s.toString().trim();
                if (TextUtils.isEmpty(text)) return;
                
                try {
                    int delay = Integer.parseInt(text);
                    if (delay >= 0) {
                        DataLoader.setDelay(delay);
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        // Clear Cache
        mCardClearCache.setOnClickListener(v -> {
            DataCleaner.cleanInternalCache(context);
            showCache();
            mTvPath.setText("尚未打开任何文件");
            ToastUtil.show(context, "缓存已清空");
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
        isUpdatingUI = true;
        
        // Display current file path
        String path = DataLoader.getLastPath();
        if (!TextUtils.isEmpty(path)) {
            int colonIndex = path.indexOf(':');
            if (colonIndex >= 0) {
                path = path.substring(colonIndex + 1);
            }
            mTvPath.setText(path);
        } else {
            mTvPath.setText("尚未打开任何文件");
        }

        // Display delay
        mEtDelay.setText(String.valueOf(DataLoader.getDelay()));

        // Set auto editor switch
        mSwitchAutoEditor.setChecked(DataLoader.autoEnterEditor());

        // Display cache size
        showCache();
        
        isUpdatingUI = false;
    }

    private void showCache() {
        try {
            String cacheSize = DataCleaner.getCacheSize(context.getCacheDir());
            mTvCache.setText("当前缓存: " + cacheSize);
        } catch (Exception e) {
            mTvCache.setText("无法计算缓存大小");
        }
    }
}
