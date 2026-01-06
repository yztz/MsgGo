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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import top.yztz.msggo.R;
import top.yztz.msggo.util.FileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MarkdownActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_ASSET_PATH = "extra_asset_path";
    private static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_RES_ID = "extra_res_id";

    /**
     * 打开 Markdown 显示界面
     *
     * @param context   上下文
     * @param title     工具栏标题
     * @param assetPath Asset 路径
     */
    public static void open(Context context, String title, String assetPath) {
        Intent intent = new Intent(context, MarkdownActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ASSET_PATH, assetPath);
        context.startActivity(intent);
    }

    public static void open(Context context, String title, int id) {
        Intent intent = new Intent(context, MarkdownActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_RES_ID, id);
        context.startActivity(intent);
    }

    /**
     * 直接通过内容打开
     */
    public static void openContent(Context context, String title, String content) {
        Intent intent = new Intent(context, MarkdownActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CONTENT, content);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String assetPath = getIntent().getStringExtra(EXTRA_ASSET_PATH);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        int res_id = getIntent().getIntExtra(EXTRA_RES_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!TextUtils.isEmpty(title)) {
            getSupportActionBar().setTitle(title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvContent = findViewById(R.id.tv_markdown_content);
        Markwon markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String finalContent;
        if (!TextUtils.isEmpty(content)) {
            finalContent = content;
        } else if (!TextUtils.isEmpty(assetPath)) {
            finalContent = loadFromAsset(assetPath);
        } else if (res_id != -1) {
            finalContent = FileUtil.loadFromRaw(this, res_id);
        } else {
            finalContent = "Failed to load content from intent.";
        }

        markwon.setMarkdown(tvContent, finalContent);
    }

    private String loadFromAsset(String basePath) {
        try {
            InputStream is = getAssets().open(basePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            return sb.toString();
        } catch (Exception e) {
            // Fallback to base path if localized version fails
            return "Failed to load content from " + basePath;
        }
    }

}
