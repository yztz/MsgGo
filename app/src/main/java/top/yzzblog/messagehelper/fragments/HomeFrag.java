package top.yzzblog.messagehelper.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import top.yzzblog.messagehelper.activities.EditActivity;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.activities.ChooserActivity;

public class HomeFrag extends Fragment {
    private Context context;
    private LinearLayout mLinearSend, mLinearEdit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_home, null);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLinearSend = view.findViewById(R.id.linear_send);
        mLinearEdit = view.findViewById(R.id.linear_edit);
        //发送按钮监听
        mLinearSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注意数据表的读入
                //注意返回结果
                if (DataLoader.getDataModel() == null) {
                    ToastUtil.show(context, "请先点击“+”导入数据哦~");
                } else {
                    if(!TextUtils.isEmpty(DataLoader.getContent())) {
                        Intent intent = new Intent(context, ChooserActivity.class);
                        startActivity(intent);
                    }else {
                        ToastUtil.show(context, "短信内容不得为空哦~");
                    }
                }
            }
        });
        //编辑按钮监听
        mLinearEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditActivity.openEditor(context);
            }
        });
    }


}
