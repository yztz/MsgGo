package top.yzzblog.messagehelper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.util.ToastUtil;


public class CoverActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private static final int REQUEST_PERMISSION = 200;
    private String[] permissions = new String[] {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allPermissionsGranted = true;

            // 检查是否所有权限都被授予
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            // 检查是否所有权限都被授予
            if (allPermissionsGranted) {
                // 权限被授予，继续正常操作
                startMain();
            } else {
                // 权限被拒绝，显示消息并退出应用
                ToastUtil.show(this, "权限被拒绝，应用将退出");
                finish(); // 退出应用
            }
        }
    }

    private void startMain() {
        Intent intent = new Intent(CoverActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            Log.d("Permissions", "开始请求权限");
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            // 所有权限已获取
            Log.d("Permissions", "所有权限已获取");
            handler.postDelayed(this::startMain, 1500);
        }

    }
}
