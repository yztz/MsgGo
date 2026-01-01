package top.yztz.msggo.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Message;
import top.yztz.msggo.util.FileUtil;

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "message_send_channel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_CANCEL = "top.yztz.msggo.action.CANCEL_SENDING";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private boolean isStopped;
    private BroadcastReceiver smsStatusReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        createForegroundNotification();
        registerSmsReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStopped = true;
        unregisterSmsReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CANCEL.equals(intent.getAction())) {
            stopSending();
            return START_NOT_STICKY;
        }

        int delay = intent.getIntExtra("delay", 5000);
        int subId = intent.getIntExtra("subId", SMSSender.getDefaultSubID());
        String serPath = intent.getStringExtra("message_file");

        if (serPath == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        SendingMonitor.getInstance().reset();
        SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.SENDING);

        new Thread(() -> {
            Message[] messages = FileUtil.readMessageArrayFromFile(getApplicationContext(), serPath);
            if (messages == null) {
                stopSelf();
                return;
            }

            SendingMonitor.getInstance().setTotal(messages.length);

            for (int i = 0; i < messages.length && !isStopped; i++) {
                Message message = messages[i];
                try {
                    SMSSender.sendMessage(getApplicationContext(), message.getContent(), message.getPhone(), subId, i + 1);

                    // Update progress (requests sent)
                    updateNotification(i + 1, messages.length);
                    SendingMonitor.getInstance().updateProgress(i + 1);

                    // Delay
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stopSending();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }

            // Clean up
            getApplicationContext().deleteFile(serPath);

            if (!isStopped) {
                SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.COMPLETED);
                showCompletedNotification();
            } else {
                SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.CANCELLED);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            } else {
                stopForeground(false);
            }
        }).start();

        return START_STICKY;
    }

    private void stopSending() {
        isStopped = true;
        SendingMonitor.getInstance().appendLog("发送任务已取消");
        SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.CANCELLED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE); // 移除通知
        } else {
            stopForeground(true);
        }

        stopSelf();
    }

    private void updateNotification(int done, int all) {
        notificationBuilder.setContentText(String.format("正在发送: %d/%d", done, all))
                .setProgress(all, done, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void showCompletedNotification() {
        notificationBuilder.setContentText("发送任务已完成")
                .setProgress(0, 0, false)
                .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createForegroundNotification() {
        Intent cancelIntent = new Intent(this, MessageService.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MsgGo 短信群发")
                .setContentText("准备发送...")
                .setSmallIcon(R.drawable.send_small)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .addAction(R.drawable.ic_close, "取消", cancelPendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Message Send Channel", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for message send progress");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void registerSmsReceiver() {
        smsStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int code = intent.getIntExtra("code", -1);
                int resultCode = getResultCode();
                String log;
                boolean success = (resultCode == android.app.Activity.RESULT_OK);

                if (success) {
                    log = "第 " + code + " 条: 发送成功";
                } else {
                    log = "第 " + code + " 条: 发送失败 (代码 " + resultCode + ")";
                }

                SendingMonitor.getInstance().appendLog(log);
            }
        };

        IntentFilter filter = new IntentFilter(SMSSender.SENT_SMS_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsStatusReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(smsStatusReceiver, filter);
        }
    }

    private void unregisterSmsReceiver() {
        if (smsStatusReceiver != null) {
            try {
                unregisterReceiver(smsStatusReceiver);
            } catch (Exception e) {
                // Ignore if not registered
            }
        }
    }
}