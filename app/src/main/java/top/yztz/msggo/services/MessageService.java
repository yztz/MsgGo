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
import androidx.core.content.ContextCompat;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Message;
import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.ToastUtil;

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "message_send_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_CANCEL = "top.yztz.msggo.action.CANCEL_SENDING";
    private static final String EXTRA_DELAY = "delay";
    private static final String EXTRA_SUB_ID = "subId";
    private static final String EXTRA_FILE_PATH = "message_file";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private boolean isStopped;
    private BroadcastReceiver smsStatusReceiver;
    private int totalMessages = 0;
    private int confirmedMessages = 0;
    private boolean allSentRequested = false;

    /**
     * Start sending messages service
     *
     * @param context  Context
     * @param messages List of messages to send
     * @param subId    Subscription ID (SIM card)
     * @param delay    Delay between messages in milliseconds
     */
    public static void startSending(Context context, java.util.List<Message> messages, int subId, int delay) {
        if (context == null || messages == null || messages.isEmpty()) return;

        // Serialize messages to file
        String serPath = FileUtil.saveMessageArrayToFile(context, messages.toArray(new Message[0]));
        if (serPath == null) {
            // Callback to UI or Toast? For now, we rely on the caller validation or just log error.
            // Since this is a void method, we might want to return boolean or throw exception.
            // But based on existing logic, we'll just try to start.
            // Actually, ChooserActivity showed a toast if save failed.
            // Let's rely on FileUtil internal error handling or assume it works for now.
            // Ideally should propagate error.
            ToastUtil.show(context, "短信服务启动失败: 文件保存错误");
            return;
        }

        Intent serviceIntent = new Intent(context, MessageService.class);
        serviceIntent.putExtra(EXTRA_DELAY, delay);
        serviceIntent.putExtra(EXTRA_SUB_ID, subId);
        serviceIntent.putExtra(EXTRA_FILE_PATH, serPath);

        ContextCompat.startForegroundService(context, serviceIntent);
    }

    /**
     * Stop sending messages
     *
     * @param context Context
     */
    public static void stopSending(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, MessageService.class);
        intent.setAction(ACTION_CANCEL);
        context.startService(intent);
    }

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

        int delay = intent.getIntExtra(EXTRA_DELAY, 5000);
        int subId = intent.getIntExtra(EXTRA_SUB_ID, SMSSender.getDefaultSubID());
        String serPath = intent.getStringExtra(EXTRA_FILE_PATH);

        if (serPath == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        totalMessages = 0;
        confirmedMessages = 0;
        allSentRequested = false;
        SendingMonitor.getInstance().reset();
        SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.SENDING);

        new Thread(() -> {
            Message[] messages = FileUtil.readMessageArrayFromFile(getApplicationContext(), serPath);
            if (messages == null) {
                stopSelf();
                return;
            }

            totalMessages = messages.length;
            SendingMonitor.getInstance().setTotal(totalMessages);
            for (int i = 0; i < messages.length && !isStopped; i++) {
                Message message = messages[i];
                try {
                    // Delay
                    if (i != 0) Thread.sleep(delay);
                    SMSSender.sendMessage(getApplicationContext(), message.getContent(), message.getPhone(), subId, i + 1);

                    // Update progress (requests sent)
                    updateNotification(i + 1, messages.length);
                    SendingMonitor.getInstance().updateSentProgress(i + 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stopSending();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }

            allSentRequested = true;

            // Clean up
            getApplicationContext().deleteFile(serPath);

            // Check if we can stop now (all confirmed)
            checkAndStopIfFinished();
        }).start();

        return START_NOT_STICKY;
    }

    private synchronized void checkAndStopIfFinished() {
        if (isStopped) return;
        
        if (allSentRequested && confirmedMessages >= totalMessages) {
            Log.i(TAG, "所有消息已发送完成并确认");
            SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.COMPLETED);
            showCompletedNotification();
            stopForeground(STOP_FOREGROUND_DETACH);
            stopSelf();
        }
    }

    private void stopSending() {
        isStopped = true;
        SendingMonitor.getInstance().appendLog("发送任务已取消");
        SendingMonitor.getInstance().setStatus(SendingMonitor.SendingState.CANCELLED);
        stopForeground(STOP_FOREGROUND_REMOVE); // 移除通知

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
                String phone = intent.getStringExtra("phone");
                int resultCode = getResultCode();
                String log;
                boolean success = (resultCode == android.app.Activity.RESULT_OK);
            
                if (success) {
                    log = String.format("#%d [%s] 发送成功", code, phone != null ? phone : "未知");
                } else {
                    log = String.format("#%d [%s] 发送失败 (代码 %d)", code, phone != null ? phone : "未知", resultCode);
                }
                Log.i(TAG, log);
                confirmedMessages++;
                SendingMonitor.getInstance().incrementConfirmed(success);
                SendingMonitor.getInstance().appendLog(log);
                checkAndStopIfFinished();
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