package top.yzzblog.messagehelper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import top.yzzblog.messagehelper.data.DataCleaner;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.util.ToastUtil;

public class SettingFrag extends Fragment {
    private Context context;
    private TextView mTvPath, mTvCache;
    private EditText mEtDelay;
    private Button mBtnSave;
    private Checkable mCbAutoEditor;
    private LinearLayout mLinearDelCache;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_setting, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvPath = view.findViewById(R.id.tv_path);
        mEtDelay = view.findViewById(R.id.et_delay);
        mBtnSave = view.findViewById(R.id.btn_save);
        mCbAutoEditor = view.findViewById(R.id.cb_auto_enter_editor);
        mTvCache = view.findViewById(R.id.tv_cache);
        mLinearDelCache = view.findViewById(R.id.linear_del_cache);

        showInfo();

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        //设置保存按钮监听
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
                //保存消息发送间隔
                int delay = Integer.parseInt(mEtDelay.getText().toString());
                if (delay <= 0) {
                    ToastUtil.show(context, "保存失败：发送间隔不能小于0");
                }
                DataLoader.setDelay(delay);

                //保存编辑器是否自动打开
                DataLoader.setAutoEnterEditor(mCbAutoEditor.isChecked());

                ToastUtil.show(context, "保存成功");
//                } catch (Exception e) {
//                    ToastUtil.show(context, "保存失败");
//                }
            }
        });

        //设置缓存按钮监听器
        mLinearDelCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataCleaner.cleanInternalCache(context);
                showCache();

                ToastUtil.show(context, "缓存已清空~");
            }
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

    public void showInfo() {
        String path = DataLoader.getLastPath();

        //设置当前路径显示
        if (TextUtils.isEmpty(path))
            mTvPath.setText("你还没加载文件呢！");
        else
            mTvPath.setText(path);

        //显示消息数限制
        mEtDelay.setText(String.valueOf(DataLoader.getDelay()));

        //设置是否进入编辑器
        mCbAutoEditor.setChecked(DataLoader.autoEnterEditor());

        //显示缓存大小
        showCache();
    }

    private void showCache() {
        try {
            mTvCache.setText(DataCleaner.getCacheSize(context.getCacheDir()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
