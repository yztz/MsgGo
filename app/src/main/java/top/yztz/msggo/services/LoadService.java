package top.yztz.msggo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import top.yztz.msggo.data.DataLoader;
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
        public final String path;
        
        public LoadStatus(boolean isLoading, boolean isSuccessful, String path) {
            this.isLoading = isLoading;
            this.isSuccessful = isSuccessful;
            this.path = path;
        }
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

                DataLoader.__load(path);

                postStatus(false, true, path);
                Log.d(TAG, "数据加载成功");
                stopSelf();
            } catch (DataLoadFailed dataLoadFailed) {
                Log.d(TAG, "数据加载失败");
                postStatus(false, false, path);
                stopSelf();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void postStatus(boolean isLoading, boolean isSuccessful, String path) {
        loadStatus.postValue(new LoadStatus(isLoading, isSuccessful, path));
    }
}
