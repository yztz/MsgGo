package top.yzzblog.messagehelper.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.exception.DataLoadFailed;

public class LoadService extends Service {
    public static final String LOADING_ACTION = "top.yzzblog.messagehelper.service.LOADING_ACTION";

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
                sendBroadcast(true, false, path);

                DataLoader.__load(path);

                sendBroadcast(false, true, path);
                Log.d("msgD", "数据加载成功");
                stopSelf();
            } catch (DataLoadFailed dataLoadFailed) {
                Log.d("msgD", "数据加载失败");
                sendBroadcast(false, false, path);
                stopSelf();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendBroadcast(boolean isLoading, boolean isSuccessful, String msg) {
        Intent it = new Intent(LOADING_ACTION);
        it.putExtra("isLoading", isLoading);
        it.putExtra("isSuccessful", isSuccessful);
        it.putExtra("path", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
//        sendBroadcast(it);
    }
}
