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

package top.yztz.msggo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import top.yztz.msggo.R;

public class XiaomiUtil {
    
    public static boolean isXiaomi() {
        return "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER) || "Xiaomi".equalsIgnoreCase(Build.BRAND);
    }

    public static Intent getPermissionManagementIntent(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.putExtra("extra_pkgname", context.getPackageName());
        
        // Check if the intent can be handled
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            return intent;
        }

        // Fallback to application details
        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    public static void showXiaomiPermissionDialog(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.xiaomi_permission_title))
                .setMessage(activity.getString(R.string.xiaomi_permission_msg))
                .setPositiveButton(activity.getString(R.string.go_to_settings), (dialog, which) -> {
                    activity.startActivity(XiaomiUtil.getPermissionManagementIntent(activity), null);
                    exitApp(activity);
                })
                .setNegativeButton(activity.getString(R.string.exit_app), (dialog, which) -> exitApp(activity))
                .setCancelable(false)
                .show();
    }

    private static void exitApp(Activity activity) {
        activity.finish();
        System.exit(0);
    }

}


