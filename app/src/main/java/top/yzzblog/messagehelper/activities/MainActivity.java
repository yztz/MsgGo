package top.yzzblog.messagehelper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;


import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.dialog.LoadDialog;
import top.yzzblog.messagehelper.fragments.HomeFrag;
import top.yzzblog.messagehelper.fragments.SettingFrag;
import top.yzzblog.messagehelper.services.LoadService;
import top.yzzblog.messagehelper.util.ToastUtil;

import static top.yzzblog.messagehelper.util.FileUtil.getPath;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 200;

    private ImageView mHome, mSet, mGet;
    private HomeFrag home;
    private SettingFrag setting;
    private LoadDialog loadDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请权限
        requestPermission();

        //更换标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View actionbarView = LayoutInflater.from(this).inflate(R.layout.layout_main_action_bar, new ConstraintLayout(this), false);
            actionBar.setCustomView(actionbarView);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toolbar parent = (Toolbar) actionbarView.getParent();
                parent.setContentInsetsAbsolute(0, 0);
            }

            ImageView mImgHelp = actionbarView.findViewById(R.id.img_help);
            mImgHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    startActivity(intent);
                }
            });
            actionBar.show();
        }

        init();

        mGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开文件浏览器
                openFileChooser();
            }
        });

        //fragment的切换
        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().hide(setting).show(home).commitAllowingStateLoss();

            }
        });

        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().hide(home).show(setting).commitAllowingStateLoss();

            }
        });

    }

    /**
     * 全局初始化
     */
    private void init() {
        mHome = findViewById(R.id.go_send);
        mSet = findViewById(R.id.go_setting);
        mGet = findViewById(R.id.go_get);
        loadDialog = new LoadDialog(this);

        registerReceiver();

        DataLoader.init(this);
        initFragment();

    }

    /**
     * 初始化fragment
     */
    public void initFragment() {
        home = new HomeFrag();
        setting = new SettingFrag();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fl_container, setting, "setting");
        transaction.hide(setting).add(R.id.fl_container, home, "home");
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data == null || data.getData() == null) return;
                String path = getPath(this, data.getData());
                if (TextUtils.isEmpty(path)) return;
                //打开excel文件
                DataLoader.load(path, this);

                break;
            default:
                break;
        }

    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS
        };
        int checkExternalStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int checkSMS = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (checkSMS != PackageManager.PERMISSION_GRANTED || checkExternalStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_PERMISSION
            );
        }
    }

    /**
     * 打开文件选择器
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {
                "application/vnd.ms-excel",
                "application/x-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        intent.setType("application/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, 1);
    }

    /**
     * 注册广播接收器
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(LoadService.LOADING_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isLoading = intent.getBooleanExtra("isLoading", false);
                if (isLoading)
                    loadDialog.show();
                else {
                    loadDialog.dismiss();
                    boolean isSuccessful = intent.getBooleanExtra("isSuccessful", false);
                    if (isSuccessful) {
                        ToastUtil.show(MainActivity.this, "数据加载成功");
                        //更新设置
                        setting.showInfo();
                        //清空编辑器
                        DataLoader.setContent("");
                        //若设置为自动进入编辑器
                        if (DataLoader.autoEnterEditor()) {
                            EditActivity.openEditor(context);
                        }
                    } else {
                        ToastUtil.show(MainActivity.this, "数据加载失败");
                    }
                }
            }
        }, filter);
    }
}

