package top.yztz.msggo.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.yztz.msggo.R;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.util.ToastUtil;

public class EditActivity extends AppCompatActivity {
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
            DataModel.setTemplate(mEt.getText().toString().trim());
            DataModel.saveAsHistory(EditActivity.this);
            ToastUtil.show(EditActivity.this, getString(R.string.save_success));
            finish();
        });

        mBottomAppBar = findViewById(R.id.bottomAppBar);
        mBottomAppBar.setOnMenuItemClickListener(v->{
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(EditActivity.this);
            switch (v.getItemId()) {
                case R.id.btn_var:
                    dialogBuilder.setTitle(getString(R.string.variable_selection_title))
                            .setItems(DataModel.getTitles(), (dialog, which) -> {
                                int loc = mEt.getSelectionStart();
                                String pat = "${" + DataModel.getTitles()[which] + "}";
                                if (loc == -1)  mEt.getText().append(pat);
                                else mEt.getText().insert(loc, pat);
                                dialog.dismiss();
                            }).setCancelable(true).show();
                    return true;
                case R.id.btn_clear:
                    dialogBuilder.setTitle(getString(R.string.confirm_clear_title))
                            .setCancelable(true).setMessage(getString(R.string.confirm_clear_msg))
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                                mEt.getText().clear();
                                dialog.dismiss();
                            }).setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                    // Respond to positive button press
                    return true;
                }
                return false;
            });


        //获取已经保存的内容并显示
        mEt.setText(DataModel.getTemplate());
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
                            .setTitle(getString(R.string.edit_unsaved_title))
                            .setMessage(getString(R.string.edit_unsaved_msg))
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                                setEnabled(false); // 禁用此回调
                                getOnBackPressedDispatcher().onBackPressed(); // 触发系统默认退出
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


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
        if (!DataModel.loaded()) {
            ToastUtil.show(context, context.getString(R.string.error_import_data_first));
        } else {
            Intent intent = new Intent(context, EditActivity.class);
            context.startActivity(intent);
        }
    }
}
