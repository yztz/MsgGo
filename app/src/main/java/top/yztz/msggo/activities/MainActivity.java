/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package top.yztz.msggo.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.widget.NestedScrollView;
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
import android.widget.ImageView;
import android.widget.TextView;

import top.yztz.msggo.R;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.data.HistoryManager;
import top.yztz.msggo.data.SettingManager;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.fragments.HomeFrag;
import top.yztz.msggo.fragments.SettingFrag;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.LocaleUtils;
import top.yztz.msggo.util.ToastUtil;
import top.yztz.msggo.util.XiaomiUtil;

import static top.yztz.msggo.util.FileUtil.getFilePathFromContentUri;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomeFrag.DataLoader {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 200;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private BottomNavigationView nMenu;
    private LinearProgressIndicator indicator;
    private ViewPager2 viewPager;
    private ImageView ivHeaderImage;
    private List<String> permissionsToRequest;

    private ActivityResultLauncher<Intent> excelPickerLauncher;

    /**
     * 初始化fragment
     */
    public void initFragment() {
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
                return position == 0 ? new HomeFrag() : new SettingFrag();
            }
        };
        
        viewPager.setAdapter(pagerAdapter);
        mCollapsingToolbarLayout.setTitle(getString(R.string.title_home));

        // Sync ViewPager with BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            boolean initiated = false;
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String newTitle = position == 0 ? getString(R.string.title_home) : getString(R.string.title_settings);
                int menuItemId = position == 0 ? R.id.nav_home : R.id.nav_settings;

                int newImageRes = position == 0 ? R.drawable.red_panda_bamboo : R.drawable.red_panda_grape;

                if (!initiated) {
                    mCollapsingToolbarLayout.setTitle(newTitle);
                    ivHeaderImage.setImageResource(newImageRes);
                    initiated = true;
                } else {
                    // Animate title
                    mCollapsingToolbarLayout.animate()
                            .alpha(0.1f)
                            .setDuration(120)
                            .withEndAction(() -> {
                                mCollapsingToolbarLayout.setTitle(newTitle);
                                mCollapsingToolbarLayout.animate()
                                        .alpha(1f)
                                        .setDuration(120)
                                        .start();
                            })
                            .start();

                    // Animate image with same animation
                    ivHeaderImage.animate()
                            .alpha(0.1f)
                            .setDuration(120)
                            .withEndAction(() -> {
                                ivHeaderImage.setImageResource(newImageRes);
                                ivHeaderImage.animate()
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

    private Fragment getCurrentFragment() {
        int currentItem = viewPager.getCurrentItem();
        return getSupportFragmentManager().findFragmentByTag("f" + currentItem);
    }


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
        Log.d(TAG, "onDestroy: reset DataModel");
        DataModel.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission: " + permissionsToRequest.get(i) + " is denied");
                    allPermissionsGranted = false;
                }
            }
            if (allPermissionsGranted) {
                Log.i(TAG, "All requested permissions granted.");
                initApp();
            } else {
                Log.w(TAG, "Some permissions were denied.");
                ToastUtil.show(this, getString(R.string.permission_denied_exit));
                finish();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i(TAG, "onNewIntent: checkShare");
        checkShare();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        SettingManager.init(this);

        LocaleUtils.applyLocale();
        excelPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        if (uri != null) {
                            Log.d(TAG, "File URI: " + uri.getEncodedPath());
                            String path = getFilePathFromContentUri(this, uri);
                            Log.i(TAG, "Importing file from picker: " + path);
                            loadData(path);
                        }
                    }
                }
        );
        setContentView(R.layout.activity_main);

        // Check Privacy Policy and Disclaimer
        if (!SettingManager.isPrivacyAccepted()) {
            showPrivacyDialog();
        } else if (!SettingManager.isDisclaimerAccepted()) {
            showDisclaimerDialog();
        } else {
            checkPermissionsAndInit();
        }
    }

    private void showPrivacyDialog() {
        showLawDialog(getString(R.string.privacy_policy), R.raw.privacy, () -> {
            SettingManager.setPrivacyAccepted(true);
            if (!SettingManager.isDisclaimerAccepted()) {
                showDisclaimerDialog();
            } else {
                checkPermissionsAndInit();
            }
        });
    }

    private void showDisclaimerDialog() {
        showLawDialog(getString(R.string.disclaimer), R.raw.disclaimer, () -> {
            SettingManager.setDisclaimerAccepted(true);
            checkPermissionsAndInit();
        });
    }

    private void showLawDialog(String title, int res_id, Runnable onAgree) {
        NestedScrollView scrollView = new NestedScrollView(this);
        TextView textView = new TextView(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding / 2, padding, padding);
        textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        textView.setLineSpacing(0, 1.2f);
        scrollView.addView(textView);

        Markwon markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String content = FileUtil.loadFromRaw(this, res_id);
//        content = content.replaceFirst("(?m)^#\\s.*(?:\\r?\\n)?", "");
        markwon.setMarkdown(textView, content);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(scrollView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.read_and_agree), (dialog, which) -> onAgree.run())
                .setNegativeButton(getString(R.string.disagree_exit), (dialog, which) -> finish())
                .show();
    }

    private void checkPermissionsAndInit() {
        nMenu = findViewById(R.id.bottom_navigation);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        indicator = findViewById(R.id.progress);
        ivHeaderImage = findViewById(R.id.iv_header_image);
        
        // Check permissions
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.SEND_SMS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.add(Manifest.permission.POST_NOTIFICATIONS);

        permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            Log.i(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            initApp();
        }
    }

    private void checkShare() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = null;

        if (Intent.ACTION_VIEW.equals(action)) {
            uri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(action)) {
            uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri.class);
        }

        if (uri != null) {
            Log.i(TAG, "load outside link: " + uri);
            loadData(FileUtil.getFilePathFromContentUri(this, uri));
        }
    }

    private void initApp() {
        // observeLoadStatus(); 
        initFragment();
        if (SMSSender.getSubs(this).isEmpty()) {
            if (XiaomiUtil.isXiaomi()) {
                Log.d(TAG, "xiaomi: check perm");
                XiaomiUtil.showXiaomiPermissionDialog(this);
                return;
            }
            ToastUtil.show(this, getString(R.string.no_sim_found_exit));
            finish();
        }
        checkShare();
    }


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
                "application/wps-office.xls",
                "application/wps-office.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        excelPickerLauncher.launch(intent);
    }

    public void loadData(String path) {
        Log.d(TAG, "Start loading path: " + path);
        if (!indicator.isShown()) {
            indicator.show();
        }

        new Thread(() -> {
            boolean isSuccess = false;
            String errorMsg = null;
            try {
                DataModel.load(path);
                isSuccess = true;
            } catch (top.yztz.msggo.exception.DataLoadFailed e) {
                if (e.res_id == R.string.unknown_error) {
                    errorMsg = getString( e.res_id, e.e.getMessage());
                } else if (e.res_id == R.string.file_too_large) {
                    errorMsg = getString(e.res_id, FileUtil.getFormatSize(Settings.EXCEL_FILE_SIZE_MAX));
                } else if (e.res_id == R.string.file_too_much_row) {
                    errorMsg = getString(e.res_id, Settings.EXCEL_ROW_COUNT_MAX);
                } else {
                    errorMsg = getString(e.res_id);
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }

            // UI updates on main thread
            boolean finalIsSuccess = isSuccess;
            String finalErrorMsg = errorMsg;
            runOnUiThread(() -> afterDataLoaded(finalIsSuccess, finalErrorMsg));
        }).start();
    }

    private void afterDataLoaded(boolean success, String errMsg) {
        if (indicator.isShown()) {
            indicator.hide();
        }

        if (!success) {
            Log.e(TAG, "Data load failed: " + errMsg);
            ToastUtil.show(MainActivity.this, getString(R.string.load_failed, errMsg));
            return;
        }

        assert DataModel.loaded();
        String path = DataModel.getPath();
        ToastUtil.show(MainActivity.this, getString(R.string.load_success));
        String currentSig = DataModel.getSignature();
        Log.i(TAG, "Data loaded successfully: " + path + " sig: " + currentSig);

        HistoryManager.HistoryItem historyItem = HistoryManager.getItem(MainActivity.this, path);
        if (historyItem != null) {
            Log.i(TAG, "Found history: " + historyItem.path + " sig: " + historyItem.signature);
            if (currentSig.equals(historyItem.signature)) {
                DataModel.setNumberColumn(historyItem.numberColumn);
                DataModel.setTemplate(historyItem.template);
                if (SMSSender.getSubBySubscriptionId(MainActivity.this, historyItem.subId) == null) {
                    ToastUtil.show(MainActivity.this, getString(R.string.unknown_sim));
                } else {
                    DataModel.setSubId(historyItem.subId);
                }
            }
        }

        String[] titles = DataModel.getTitles();
        if (!TextUtils.isEmpty(DataModel.getNumberColumn())) {
            Log.i(TAG, "Use existing history");
            DataModel.saveAsHistory(MainActivity.this);
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof HomeFrag) {
                ((HomeFrag)fragment).updateStatus();
            }
        } else if (titles != null && titles.length > 0) {
            Log.i(TAG, "Prompt for column selection");
            int checkedItem = -1;
            String currentColumn = DataModel.getNumberColumn();
            for (int i = 0; i < titles.length; i++) {
                if (titles[i].equals(currentColumn)) {
                    checkedItem = i;
                    break;
                }
            }
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.select_number_column_dialog_title))
                    .setSingleChoiceItems(titles, checkedItem, (dialog, which) -> {
                        DataModel.setNumberColumn(titles[which]);
                        DataModel.saveAsHistory(MainActivity.this);
                        Fragment fragment = getCurrentFragment();
                        if (fragment instanceof HomeFrag) {
                            ((HomeFrag)fragment).updateStatus();
                        }
                        dialog.dismiss();
                        if (SettingManager.autoEnterEditor()) {
                            EditActivity.openEditor(MainActivity.this);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else if (SettingManager.autoEnterEditor()) {
            EditActivity.openEditor(MainActivity.this);
        }
    }

}
