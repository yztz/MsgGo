package top.yzzblog.messagehelper.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
// import android.widget.TextSwitcher; // Removed

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.data.DataContext;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.data.HistoryManager;
import top.yzzblog.messagehelper.fragments.HomeFrag;
import top.yzzblog.messagehelper.fragments.SettingFrag;
import top.yzzblog.messagehelper.services.LoadService;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.FileUtil;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.util.XiaomiUtil;

import static top.yzzblog.messagehelper.util.FileUtil.getFilePathFromContentUri;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 200;
    private final String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SEND_SMS
    };

    private HomeFrag home;
    private SettingFrag setting;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private BottomNavigationView nMenu;
    private LinearProgressIndicator indicator;

    private ActivityResultLauncher<Intent> excelPickerLauncher;

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        // Save current state to history
//        String lastPath = DataLoader.getLastPath();
//        if (!TextUtils.isEmpty(lastPath)) {
//            HistoryManager.addHistory(this, lastPath, DataLoader.getContent(), DataLoader.getNumberColumn(), DataLoader.getLastSignature());
//        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                initApp();
            } else {
                ToastUtil.show(this, "权限被拒绝，应用将退出");
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        excelPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        if (uri != null) {
                            Log.d(TAG, "File URI: " + uri.getEncodedPath());
                            // 调用你原来的路径转换和加载工具
                            String path = getFilePathFromContentUri(this, uri);
                            DataLoader.load(path, this);
                        }
                    }
                }
        );
        setContentView(R.layout.activity_main);
        observeLoadStatus();
        nMenu = findViewById(R.id.bottom_navigation);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        indicator = findViewById(R.id.progress);
        
//        DialogX.init(this);

        // Check permissions
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            Log.d(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            initApp();
        }
    }

    private void initApp() {
        DataLoader.init(this);
        initFragment();
        if (SMSSender.getSubs(this).isEmpty()) {
            if (XiaomiUtil.isXiaomi()) {
                Log.d(TAG, "xiaomi: check perm");
                XiaomiUtil.showXiaomiPermissionDialog(this);
                return;
            }
            ToastUtil.show(this, "没发现可用于发送短信的 SIM 卡，即将退出");
            finish();
        }
    }




    /**
     * 初始化fragment
     */
    public void initFragment() {
        home = new HomeFrag();
        setting = new SettingFrag();

        mCollapsingToolbarLayout.setTitle("送 信");
        loadFragment(home);

        nMenu.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                mCollapsingToolbarLayout.setTitle("送 信");
                selectedFragment = home;
            } else if (itemId == R.id.nav_settings) {
                mCollapsingToolbarLayout.setTitle("設 定");
                selectedFragment = setting;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            if (data == null || data.getData() == null) return;
//            Uri uri = data.getData();
//            Log.d(TAG, "File URI: " + uri.getEncodedPath());
//            String path = getFilePathFromContentUri(this, data.getData());
//            DataLoader.load(path, this);
//        }
//    }

    /**
     * 打开文件选择器
     */
    public void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");

        String[] mimeTypes = {
                "application/vnd.ms-excel",
                "application/x-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        excelPickerLauncher.launch(intent);
    }

    /**
     * 使用 LiveData 观察加载状态
     */
    private void observeLoadStatus() {
        LoadService.getLoadStatus().observe(this, status -> {
            if (status == null) return;
            
            if (status.isLoading) {
                if (!indicator.isShown()) {
                    indicator.show();
                }
            } else {
                if (indicator.isShown()) {
                    indicator.hide();
                }

                
                if (status.isSuccessful) {
                    DataLoader.setLastPath(status.path);
                    String currentSig = DataLoader.getLastSignature();
                    Log.i(TAG, "数据加载成功：" + status.path + " 签名: " + currentSig);
                    ToastUtil.show(MainActivity.this, "数据加载成功");

                    DataContext historyItem = HistoryManager.getItem(MainActivity.this, status.path);
                    if (historyItem != null) {
                        Log.i(TAG, "发现历史数据：" + historyItem.path + " 签名: " + historyItem.signature);
                        if (currentSig.equals(historyItem.signature)) {
                            DataLoader.setNumberColumn(historyItem.numberColumn);
                            DataLoader.setContent(historyItem.template);
                        } else {
                             DataLoader.setNumberColumn("");
                             DataLoader.setContent("");
                        }
                    }

                    String[] titles = DataLoader.getTitles();
                    if (!TextUtils.isEmpty(DataLoader.getNumberColumn())) {
                        HistoryManager.addHistory(MainActivity.this, status.path, DataLoader.getContent(), DataLoader.getNumberColumn(), currentSig);
                        setting.showInfo();
                        home.updateStatus();
                    } else if (titles != null && titles.length > 0) {
                        int checkedItem = -1;
                        String currentColumn = DataLoader.getNumberColumn();
                        for (int i = 0; i < titles.length; i++) {
                            if (titles[i].equals(currentColumn)) {
                                checkedItem = i;
                                break;
                            }
                        }

                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("选择号码列")
                                .setSingleChoiceItems(titles, checkedItem, (dialog, which) -> {
                                    DataLoader.setNumberColumn(titles[which]);
                                    // Update history with new selection
                                    HistoryManager.addHistory(MainActivity.this, status.path, DataLoader.getContent(), titles[which], currentSig);
                                    setting.showInfo();
                                    home.updateStatus();
                                    dialog.dismiss();
                                    if (DataLoader.autoEnterEditor()) {
                                        EditActivity.openEditor(MainActivity.this);
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    } else if (DataLoader.autoEnterEditor()) {
                        EditActivity.openEditor(this);
                    }
                } else {
                    ToastUtil.show(MainActivity.this, "数据加载失败");
                }
            }
        });
    }
}
