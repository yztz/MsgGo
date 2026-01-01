package top.yztz.msggo.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.yztz.msggo.R;
import top.yztz.msggo.adapters.VarAdapter;
import top.yztz.msggo.data.DataContext;
import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.util.ToastUtil;

public class EditActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private EditText mEt;
//    private DrawerLayout mDrawerLayout;
    private BottomAppBar mBottomAppBar;
    private FloatingActionButton mBtnSave;
    private boolean edited;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        mEt = findViewById(R.id.et_editor);
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(v->{
            DataLoader.setContent(mEt.getText().toString().trim());
            HistoryManager.addHistory(this, DataLoader.getDataContext());
            ToastUtil.show(EditActivity.this, "信息保存成功！");
            finish();
        });

        mBottomAppBar = findViewById(R.id.bottomAppBar);
        mBottomAppBar.setOnMenuItemClickListener(v->{
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
            switch (v.getItemId()) {
                case R.id.btn_var:
                    dialogBuilder.setTitle("变量选取")
                            .setItems(DataLoader.getTitles(), (dialog, which) -> {
                                int loc = mEt.getSelectionStart();
                                String pat = "${" + DataLoader.getTitles()[which] + "}";
                                if (loc == -1)  mEt.getText().append(pat);
                                else mEt.getText().insert(loc, pat);
                                dialog.dismiss();
                            }).setCancelable(true).show();
                    return true;
                case R.id.btn_clear:
                    dialogBuilder.setTitle("确认清空")
                            .setCancelable(true).setMessage("你的编辑将会丢失很久，真的很久很久...")
                            .setPositiveButton("确定", (dialog, which) -> {
                                mEt.getText().clear();
                                dialog.dismiss();
                            }).setNegativeButton("取消", (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                    // Respond to positive button press
                    return true;
                }
                return false;
            });


        //获取已经保存的内容并显示
        mEt.setText(DataLoader.getContent());
        highLight(mEt.getText());


        //自动高亮变量
        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //高亮显示
                highLight(s);
                edited = true;
            }
        });
        //线性布局
//        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
//        mRv.setLayoutManager(manager);
//        //分割线
//        mRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        mRv.setAdapter(adapter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (edited) {
                    new MaterialAlertDialogBuilder(EditActivity.this)
                            .setTitle("您的编辑尚未保存哦")
                            .setMessage("确定不保存就离开吗？")
                            .setPositiveButton("确定", (dialog, which) -> {
                                setEnabled(false); // 禁用此回调
                                getOnBackPressedDispatcher().onBackPressed(); // 触发系统默认退出
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * 高亮
     *
     * @param s
     */
    private void highLight(Editable s) {
        String temp = s.toString();
        CharacterStyle span;

        Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher m = p.matcher(temp);
        while (m.find()) {
            span = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.md_theme_primary));
            s.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && edited) {
//            new MaterialAlertDialogBuilder(this).setTitle("您的编辑尚未保存哦")
//                    .setMessage("确定不保存就离开吗？")
//                    .setPositiveButton("确定", (dialog, which) -> {
//                        dialog.dismiss();
//                        finish();
//                    }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * 打开编辑器
     */
    public static void openEditor(Context context) {
        if (DataLoader.getDataModel() == null) {
            ToastUtil.show(context, "请先点击“+”导入数据哦~");
        } else {
            Intent intent = new Intent(context, EditActivity.class);
            context.startActivity(intent);
        }
    }
}
