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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import top.yztz.msggo.BuildConfig;
import top.yztz.msggo.R;
import top.yztz.msggo.util.ToastUtil;

public class AboutActivity extends AppCompatActivity {

    private int easterEggClickCount = 0;
    private static final int EASTER_EGG_THRESHOLD = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));

        // Show Arch
//        TextView tvArch = findViewById(R.id.tv_arch);
//        tvArch.setText(android.os.Build.SUPPORTED_ABIS[0]);

        // Easter egg: tap app icon 5 times
        ImageView ivAppIcon = findViewById(R.id.iv_app_icon);
        ivAppIcon.setOnClickListener(v -> {
            easterEggClickCount++;
            // Show watermelons: 1, 2, 3, 4, 5
            StringBuilder watermelons = new StringBuilder();
            for (int i = 0; i < Math.min(easterEggClickCount, EASTER_EGG_THRESHOLD); i++) {
                watermelons.append("🍉");
            }
            ToastUtil.show(this, watermelons.toString());
            
            if (easterEggClickCount >= EASTER_EGG_THRESHOLD) {
                ivAppIcon.setImageResource(R.drawable.red_panda_watermelon);
                easterEggClickCount = 0; // Reset for next time
            }
        });

        findViewById(R.id.row_source_code).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.visit_source_code))
                .setMessage(getString(R.string.going_to_url, "https://github.com/yztz/MsgGo"))
                .setPositiveButton(getString(R.string.visit), (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yztz/MsgGo"));
                        startActivity(intent);
                    } catch (Exception e) {
                        ToastUtil.show(this, getString(R.string.cannot_open_link, e.getMessage()));
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show());

        findViewById(R.id.row_privacy_policy).setOnClickListener(v ->
                MarkdownActivity.open(this, getString(R.string.privacy_policy), R.raw.privacy));

        findViewById(R.id.row_disclaimer).setOnClickListener(v ->
                MarkdownActivity.open(this, getString(R.string.disclaimer), R.raw.disclaimer));
    }

}

