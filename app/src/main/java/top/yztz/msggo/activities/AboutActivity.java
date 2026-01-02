package top.yztz.msggo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.yztz.msggo.BuildConfig;
import top.yztz.msggo.R;
import top.yztz.msggo.util.Config;
import top.yztz.msggo.util.ToastUtil;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvVersion = findViewById(R.id.tv_version);
//        TextView tvBuildDate = findViewById(R.id.tv_build_date);
        
        tvVersion.setText("Version " + BuildConfig.VERSION_NAME);
        
        // Format build date
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        // Note: BuildConfig.BUILD_TIME is expected to be a long
//        // If it's not generated yet, this might error in IDE but works after build.
//        // We will assume the Gradle change is applied.
//        try {
//             // Accessing the field reflectively or directly if generated.
//             // Since we modified build.gradle, it should be available as BuildConfig.BUILD_TIME
//             // However, for safety in case of sync issues, we'll try/catch or just use it.
//             // Direct access:
//             Date buildDate = new Date(BuildConfig.BUILD_TIME);
//             tvBuildDate.setText("Built on " + sdf.format(buildDate));
//        } catch (Exception e) {
//            tvBuildDate.setText("Build Date Unknown");
//        }

        // Show Arch
        TextView tvArch = findViewById(R.id.tv_arch);
        tvArch.setText(android.os.Build.SUPPORTED_ABIS[0]);

        findViewById(R.id.row_source_code).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                .setTitle("访问源代码")
                .setMessage("即将前往 https://github.com/yztz/MsgGo")
                .setPositiveButton("访问", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yztz/MsgGo"));
                        startActivity(intent);
                    } catch (Exception e) {
                        ToastUtil.show(this, "无法打开链接: " + e.getMessage());
                    }
                })
                .setNegativeButton("取消", null)
                .show());

        findViewById(R.id.row_privacy_policy).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                .setTitle("隐私政策")
                .setMessage(Config.PRIVACY_POLICY)
                .setPositiveButton("确定", null)
                .show());

        findViewById(R.id.row_disclaimer).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                .setTitle("免责声明")
                .setMessage(Config.DISCLAIMER)
                .setPositiveButton("确定", null)
                .show());
    }

}
