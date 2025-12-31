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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextSwitcher;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.fragments.HomeFrag;
import top.yzzblog.messagehelper.fragments.SettingFrag;
import top.yzzblog.messagehelper.services.LoadService;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.ToastUtil;

import static top.yzzblog.messagehelper.util.FileUtil.getFilePathFromContentUri;

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
    private TextSwitcher mTitle;
    private BottomNavigationView nMenu;
    private LinearProgressIndicator indicator;
    private String lastProcessedPath = null;

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
        mTitle = findViewById(R.id.title);
        indicator = findViewById(R.id.progress);
        mTitle.setInAnimation(this, R.anim.fade_in);
        mTitle.setOutAnimation(this, R.anim.fade_out);
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
        initImporter();
        if (SMSSender.getSubs(this).isEmpty()) {
            ToastUtil.show(this, "没发现可用于发送短信的 SIM 卡，即将退出");
            finish();
        }

    }


    @SuppressLint("RestrictedApi")
    private void initImporter() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) nMenu.getChildAt(0);
        android.view.View centerButton = menuView.getChildAt(1);
        BottomNavigationItemView itemView = (BottomNavigationItemView) centerButton;
        itemView.setShifting(false);
        itemView.setCheckable(false);
        itemView.setOnClickListener(_view -> openFileChooser());

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
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                mTitle.setText("送 信");
                selectedFragment = home;
            } else if (itemId == R.id.nav_settings) {
                mTitle.setText("設 定");
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
    private void openFileChooser() {
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
                
                // Prevent duplicate processing of the same load event
                if (status.path != null && status.path.equals(lastProcessedPath)) {
                    return;
                }
                lastProcessedPath = status.path;
                
                if (status.isSuccessful) {
                    ToastUtil.show(MainActivity.this, "数据加载成功");
                    DataLoader.setLastPath(status.path);
                    if (DataLoader.autoEnterEditor()) {
                        EditActivity.openEditor(this);
                    }
                } else {
                    ToastUtil.show(MainActivity.this, "数据加载失败");
                }
            }
        });
    }
}
