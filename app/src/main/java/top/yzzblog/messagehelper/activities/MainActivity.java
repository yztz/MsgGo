package top.yzzblog.messagehelper.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.transition.TransitionManager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;


import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.dialog.LoadDialog;
import top.yzzblog.messagehelper.fragments.HomeFrag;
import top.yzzblog.messagehelper.fragments.SettingFrag;
import top.yzzblog.messagehelper.services.LoadService;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.Config;
import top.yzzblog.messagehelper.util.ToastUtil;

import static top.yzzblog.messagehelper.util.FileUtil.getFilePathFromContentUri;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.transition.MaterialFade;
import com.kongzue.dialogx.DialogX;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
//    private ImageView mHome, mSet, mGet;
    private HomeFrag home;
    private SettingFrag setting;
//    private LoadDialog loadDialog;
    private TextSwitcher mTitle;
    private BottomNavigationView nMenu;
    private LinearProgressIndicator indicator;
    private BroadcastReceiver loadReceiver;

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterReceiver();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
//        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        setContentView(R.layout.activity_main);
        nMenu = findViewById(R.id.bottom_navigation);
        mTitle = findViewById(R.id.title);
        indicator = findViewById(R.id.progress);
        mTitle.setInAnimation(this, R.anim.fade_in);
        mTitle.setOutAnimation(this, R.anim.fade_out);
//        loadDialog = new LoadDialog(this);
        DialogX.init(this);
        initImporter();


        if (SMSSender.getSubs(this).isEmpty()) {
            ToastUtil.show(this, "没发现可用于发送短信的 SIM 卡，即将退出");
            finish(); // 退出应用
        }

    }


    @SuppressLint("RestrictedApi")
    private void initImporter() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) nMenu.getChildAt(0);
        View centerButton = menuView.getChildAt(1);
        BottomNavigationItemView itemView = (BottomNavigationItemView) centerButton;
        itemView.setShifting(false);
        itemView.setCheckable(false);
        itemView.setOnClickListener(_view -> {
            openFileChooser();
        });

        DataLoader.init(this);
        initFragment();
    }

    /**
     * 初始化fragment
     */
    public void initFragment() {
        home = new HomeFrag();
        setting = new SettingFrag();

        mTitle.setText("送 信");
        loadFragment(home);

        nMenu.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    mTitle.setText("送 信");
                    selectedFragment = home;
                    break;
                case R.id.nav_settings:
                    mTitle.setText("設 定");
                    selectedFragment = setting;
                    break;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.fl_container, setting, "setting");
//        transaction.hide(setting).add(R.id.fl_container, home, "home");
//        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data == null || data.getData() == null) return;
                //String path = getPath(this, data.getData());
                Uri uri = data.getData();
//                Log.d("msgD", "onActivityResult: "+ uri.toString()+ " " + uri.getLastPathSegment()+ " " +uri.getPath());
                Log.d("msgD", uri.getEncodedPath());
                String path = getFilePathFromContentUri(this, data.getData());
                //打开excel文件
                DataLoader.load(path, this);

                break;
            default:
                break;
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

    private void unRegisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loadReceiver);
    }

    /**
     * 注册广播接收器
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(LoadService.LOADING_ACTION);
        loadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isLoading = intent.getBooleanExtra("isLoading", false);
                if (isLoading && !indicator.isShown()) {
                    indicator.show();
                } else if (!isLoading) {
                    if (indicator.isShown()) {
                        indicator.hide();
                    }
//                    loadDialog.dismiss();
                    boolean isSuccessful = intent.getBooleanExtra("isSuccessful", false);
                    if (isSuccessful) {
                        ToastUtil.show(MainActivity.this, "数据加载成功");
                        DataLoader.setLastPath(intent.getStringExtra("path"));
                        //更新设置
//                        setting.showInfo();
                        //若设置为自动进入编辑器
                        if (DataLoader.autoEnterEditor()) {
                            EditActivity.openEditor(context);
                        }
                    } else {
                        ToastUtil.show(MainActivity.this, "数据加载失败");
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(loadReceiver, filter);
    }
}

