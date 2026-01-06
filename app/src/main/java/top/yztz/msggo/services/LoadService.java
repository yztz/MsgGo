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

package top.yztz.msggo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.exception.DataLoadFailed;

public class LoadService extends Service {
    private static final String TAG = "LoadService";
    
    // LiveData for observing load status
    private static final MutableLiveData<LoadStatus> loadStatus = new MutableLiveData<>();
    
    public static MutableLiveData<LoadStatus> getLoadStatus() {
        return loadStatus;
    }
    
    public static class LoadStatus {
        public final boolean isLoading;
        public final boolean isSuccessful;
        public final String errorMsg;
        public final String path;
        
        public LoadStatus(boolean isLoading, boolean isSuccessful, String path, String errorMsg) {
            this.isLoading = isLoading;
            this.isSuccessful = isSuccessful;
            this.errorMsg = errorMsg;
            this.path = path;
        }
    }

    public static void load(Context context, String path) {
        Log.d(TAG, "开始加载path: " + path);
        Intent intent = new Intent(context, LoadService.class);
        intent.putExtra("path", path);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final String path = intent.getStringExtra("path");

        new Thread(() -> {
            try {
                postStatus(true, false, path);

                java.io.File file = new java.io.File(path);
                if (file.exists() && file.length() > top.yztz.msggo.util.FileUtil.MAX_FILE_SIZE) {
                    throw new DataLoadFailed(getApplicationContext().getString(top.yztz.msggo.R.string.file_too_large));
                }

                DataModel.load(getApplicationContext(), path);

                postStatus(false, true, path);
                Log.d(TAG, "数据加载成功");
                stopSelf();
            } catch (DataLoadFailed dataLoadFailed) {
                Log.d(TAG, "数据加载失败: " + dataLoadFailed.msg);
                postStatus(false, false, path, dataLoadFailed.msg);
                stopSelf();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void postStatus(boolean isLoading, boolean isSuccessful, String path) {
        loadStatus.postValue(new LoadStatus(isLoading, isSuccessful, path, "null"));
    }

    private void postStatus(boolean isLoading, boolean isSuccessful, String path, String msg) {
        loadStatus.postValue(new LoadStatus(isLoading, isSuccessful, path, msg));
    }
}
