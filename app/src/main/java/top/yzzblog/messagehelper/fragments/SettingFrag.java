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

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.util.ToastUtil;

public class SettingFrag extends Fragment {
    private Context context;
    private TextView mTvPath;
    private EditText mEtMaxLimit;
    private Button mBtnSave;
    private Checkable mCbAutoEditor;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_setting, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvPath = view.findViewById(R.id.tv_path);
        mEtMaxLimit = view.findViewById(R.id.et_max_limit);
        mBtnSave = view.findViewById(R.id.btn_save);
        mCbAutoEditor = view.findViewById(R.id.cb_auto_enter_editor);

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
                try {
                    //保存消息限制数
                    int limit_num = Integer.parseInt(mEtMaxLimit.getText().toString());
                    DataLoader.setMaxLimit(limit_num);

                    //保存编辑器是否自动
                    DataLoader.setAutoEnterEditor(mCbAutoEditor.isChecked());

                    ToastUtil.show(context, "保存成功");
                } catch (NumberFormatException e) {
                    ToastUtil.show(context, "保存失败，错误的限制数目");
                }
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
            mTvPath.setText("你还没选择路径呢！");
        else
            mTvPath.setText(path);

        //显示消息数限制
        mEtMaxLimit.setText(String.valueOf(DataLoader.getMaxLimit()));

        //设置是否进入编辑器
        mCbAutoEditor.setChecked(DataLoader.autoEnterEditor());
    }

}
