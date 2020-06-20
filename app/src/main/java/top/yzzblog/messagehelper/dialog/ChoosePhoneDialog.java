package top.yzzblog.messagehelper.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.adapters.VarAdapter;

public class ChoosePhoneDialog extends Dialog {
    private RecyclerView mRv;
    private Context context;

    public ChoosePhoneDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_choose_phone_colunm);
        //设置宽度
        WindowManager m = getWindow().getWindowManager();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Display d = m.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) (size.x * 0.8);
        getWindow().setAttributes(p);

        mRv = findViewById(R.id.rv_vars);

        VarAdapter adapter = new VarAdapter(context, new VarAdapter.IOnClickListener() {
            @Override
            public void perform(int position) {
                DataLoader.setNumberColumn(DataLoader.getTitles()[position]);
                dismiss();
            }
        });

        RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
        mRv.setLayoutManager(manager);
        mRv.setAdapter(adapter);
    }
}
