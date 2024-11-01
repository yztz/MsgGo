package top.yzzblog.messagehelper.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.data.Message;
import top.yzzblog.messagehelper.util.FileUtil;



public class MessageService extends Service {
    private static final String TAG = "SMSService";
    private static final String CHANNEL_ID = "message_send_channel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private boolean isStopped;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        createForegroundNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStopped = true;  // 停止发送短信
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 获取传递的参数
        int delay = intent.getIntExtra("delay", 5000);
        int subId = intent.getIntExtra("subId", SMSSender.getDefaultSubID());
        String serPath = intent.getStringExtra("message_file");

        if (serPath == null) {
            Log.d(TAG, "serPath is null");
            stopSelf();  // 停止服务
            return START_NOT_STICKY;
        }

        // 启动前台通知，让服务在后台运行
        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        // 在后台线程中发送短信
        new Thread(() -> {
            Message[] messages = FileUtil.readMessageArrayFromFile(getApplicationContext(), serPath);
            if (messages == null) {
                Log.d(TAG, "messages is null");
                stopSelf();
                return;
            }

            for (int i = 0; i < messages.length && !isStopped; i++) {
                Message message = messages[i];
                try {
                    Log.d(TAG, String.format("Sending message-%d to %s, content: %s", i, message.getPhone(), message.getContent()));
                    SMSSender.sendMessage(getApplicationContext(), message.getContent(), message.getPhone(), subId, i + 1);

                    updateProgress(100 * i / messages.length, i + 1, messages.length);
                    // 模拟发送短信的延迟
                    Thread.sleep(delay);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stopSelf();  // 出现错误时停止服务
                    return;
                }
            }

            // 删除文件
            boolean isDeleted = getApplicationContext().deleteFile(serPath);
            if (isDeleted) {
                Log.d(TAG, "File " + serPath + " has been deleted.");
            } else {
                Log.d(TAG, "Failed to delete file: " + serPath);
            }

            stopSelf();  // 完成后停止服务

        }).start();

        return START_STICKY;  // 服务在后台可重启
    }

    private void updateProgress(int progress, int done, int all) {
        notificationBuilder.setProgress(100, progress, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.setContentText(String.format("%d/%d", done, all)).build());
    }

    // 创建前台通知
    private void createForegroundNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("短信发送中")
                .setSmallIcon(R.drawable.send_small)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationBuilder = builder;
    }

    // 创建通知渠道 (适用于 Android 8.0 及以上)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Message Send Channel";
            String description = "Channel for message send progress";
            int importance = NotificationManager.IMPORTANCE_HIGH;  // 不要干扰用户
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
