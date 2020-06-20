package top.yzzblog.messagehelper.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.adapters.VarAdapter;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.util.ToastUtil;

public class EditActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private EditText mEt;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mEt = findViewById(R.id.et_editor);
        mRv = findViewById(R.id.rv_vars);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        //更换标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View actionbarView = LayoutInflater.from(this).inflate(R.layout.layout_edit_action_bar, new ConstraintLayout(this), false);
            actionBar.setCustomView(actionbarView);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toolbar parent = (Toolbar) actionbarView.getParent();
                parent.setContentInsetsAbsolute(0, 0);
            }
            ImageView mImgContent = actionbarView.findViewById(R.id.img_content);
            Button mBtnSave = actionbarView.findViewById(R.id.btn_save);
            //保存按钮监听器
            mBtnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //保存当前编辑消息
                    DataLoader.setContent(mEt.getText().toString());
                    ToastUtil.show(EditActivity.this, "信息保存成功！");
                    finish();
                }
            });
            //设置侧栏按钮的响应事件
            mImgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                        mDrawerLayout.closeDrawer(GravityCompat.START, true);
                    else
                        mDrawerLayout.openDrawer(GravityCompat.START, true);
                }
            });
        }
        //获取已经保存的内容并显示
        mEt.setText(DataLoader.getContent());
        highLight(mEt.getText());

        //变量查询列表适配器初始化
        VarAdapter adapter = new VarAdapter(
                getApplicationContext(),
                new VarAdapter.IOnClickListener() {
                    @Override
                    public void perform(int position) {
                        //点击事件接口
                        mEt.append("${" + DataLoader.getTitles()[position] + "}");
                        mDrawerLayout.closeDrawer(GravityCompat.START, true);
                    }
                });
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
            }
        });
        //线性布局
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRv.setLayoutManager(manager);
        //分割线
        mRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRv.setAdapter(adapter);
    }

    /**
     * 高亮
     *
     * @param s
     */
    private static void highLight(Editable s) {
        String temp = s.toString();
        CharacterStyle span;

        Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher m = p.matcher(temp);
        while (m.find()) {
            span = new ForegroundColorSpan(Color.RED);
            s.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 屏蔽返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //屏蔽返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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
