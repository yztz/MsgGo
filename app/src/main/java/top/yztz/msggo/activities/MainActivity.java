package top.yztz.msggo.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import top.yztz.msggo.R;
import top.yztz.msggo.data.DataContext;
import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.fragments.HomeFrag;
import top.yztz.msggo.fragments.SettingFrag;
import top.yztz.msggo.services.LoadService;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.Config;
import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.ToastUtil;
import top.yztz.msggo.util.XiaomiUtil;

import static top.yztz.msggo.util.FileUtil.getFilePathFromContentUri;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 200;

    private HomeFrag home;
    private SettingFrag setting;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private BottomNavigationView nMenu;
    private LinearProgressIndicator indicator;
    private ViewPager2 viewPager;

    private ActivityResultLauncher<Intent> excelPickerLauncher;

    /**
     * 初始化fragment
     */
    public void initFragment() {
        home = new HomeFrag();
        setting = new SettingFrag();

        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(true); // Enable swipe
        
        // Create adapter for ViewPager2
        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            @NonNull
            public Fragment createFragment(int position) {
                return position == 0 ? home : setting;
            }
        };
        
        viewPager.setAdapter(pagerAdapter);
        mCollapsingToolbarLayout.setTitle("送 信");

        // Sync ViewPager with BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            boolean initiated = false;
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String newTitle = position == 0 ? "送 信" : "設 定";
                int menuItemId = position == 0 ? R.id.nav_home : R.id.nav_settings;

                if (!initiated) {
                    mCollapsingToolbarLayout.setTitle(newTitle);
                    initiated = true;
                } else {
                    mCollapsingToolbarLayout.animate()
                            .alpha(0.1f)
                            .setDuration(120)
                            .withEndAction(() -> {
                                mCollapsingToolbarLayout.setTitle(newTitle);
                                // Slide in and fade new title
                                mCollapsingToolbarLayout.animate()
                                        .alpha(1f)
                                        .setDuration(120)
                                        .start();
                            })
                            .start();
                }

                
                // Update bottom navigation
                nMenu.setSelectedItemId(menuItemId);
            }
        });

        nMenu.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int position = itemId == R.id.nav_home ? 0 : 1;
            viewPager.setCurrentItem(position, true);
            return true;
        });
    }

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
                            String path = getFilePathFromContentUri(this, uri);
                            DataLoader.load(path, this);
                        }
                    }
                }
        );
        setContentView(R.layout.activity_main);
        DataLoader.init(this);

        // Check Privacy Policy and Disclaimer
        if (!DataLoader.isPrivacyAccepted()) {
            showPrivacyDialog();
        } else if (!DataLoader.isDisclaimerAccepted()) {
            showDisclaimerDialog();
        } else {
            checkPermissionsAndInit();
        }
    }

    private void showPrivacyDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("隐私政策")
                .setMessage(Config.PRIVACY_POLICY)
                .setCancelable(false)
                .setPositiveButton("同意并继续", (dialog, which) -> {
                    DataLoader.setPrivacyAccepted(true);
                    if (!DataLoader.isDisclaimerAccepted()) {
                        showDisclaimerDialog();
                    } else {
                        checkPermissionsAndInit();
                    }
                })
                .setNegativeButton("不同意并退出", (dialog, which) -> finish())
                .show();
    }

    private void showDisclaimerDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("免责声明")
                .setMessage(Config.DISCLAIMER)
                .setCancelable(false)
                .setPositiveButton("已阅读并同意", (dialog, which) -> {
                    DataLoader.setDisclaimerAccepted(true);
                    checkPermissionsAndInit();
                })
                .setNegativeButton("不同意并退出", (dialog, which) -> finish())
                .show();
    }

    private void checkPermissionsAndInit() {
        observeLoadStatus();
        nMenu = findViewById(R.id.bottom_navigation);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        indicator = findViewById(R.id.progress);
        
        // Check permissions
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.SEND_SMS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.add(Manifest.permission.POST_NOTIFICATIONS);

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
        // check share
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = null;

        if (Intent.ACTION_VIEW.equals(action)) {
            uri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(action)) {
            uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri.class);
        }

        if (uri != null) {
            Log.i(TAG, "检测到外部链接，加载: " + uri);
            DataLoader.load(FileUtil.getFilePathFromContentUri(this, uri), this);
        }
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
                    ToastUtil.show(MainActivity.this, "数据加载失败: " + status.errorMsg);
                }
            }
        });
    }
}
