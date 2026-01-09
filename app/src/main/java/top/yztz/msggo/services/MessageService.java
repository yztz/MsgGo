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

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Message;

/**
 * Lightweight service for sending individual SMS messages.
 * The sending loop and delay control are managed by SendingActivity.
 */
public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "message_send_channel";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private final IBinder binder = new LocalBinder();
    private Callback callback = null;
    private final BroadcastReceiver smsStatusReceiver = new SMSBroadcastReceiver((code, success) -> {
        if (callback != null) callback.onMessageConfirmed(code, success);
    });
    
    private int totalMessages = 0;
    private int submittedCount = 0;

    public class LocalBinder extends Binder {
        public MessageService getService() {
            return MessageService.this;
        }
    }

    public interface Callback {
        void onMessageSubmitted(int index);
        void onMessageConfirmed(int index, boolean success);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        createForegroundNotification();
        registerSmsReceiver();
        Log.d(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSmsReceiver();
        Log.d(TAG, "Service destroyed");
    }

    /**
     * Initialize for a new sending session. Call before sending any messages.
     */
    public void initSession(int total) {
        this.totalMessages = total;
        this.submittedCount = 0;
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        Log.i(TAG, "Session initialized. Total messages: " + total);
    }

    /**
     * Send a single message immediately.
     */
    public void sendOne(Message message, int index, int subId) {
        SMSSender.sendMessage(getApplicationContext(), message.getContent(), message.getPhone(), subId, index);
        submittedCount++;
        updateNotification(submittedCount, totalMessages);
        if (callback != null) {
            callback.onMessageSubmitted(index);
        }
        Log.d(TAG, "Submitted message " + (index + 1) + "/" + totalMessages);
    }

    /**
     * Update notification to show paused state.
     */
    public void notifyPaused() {
        notificationBuilder.setContentText(getString(R.string.paused));
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Update notification to show resumed/sending state.
     */
    public void notifyResumed() {
        updateNotification(submittedCount, totalMessages);
    }

    /**
     * Complete the sending session.
     */
    public void finishSession(boolean completed) {
        if (completed) {
            showCompletedNotification();
            stopForeground(STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        stopSelf();
    }

    private void updateNotification(int done, int all) {
        notificationBuilder.setContentText(getString(R.string.notification_progress_format, done, all))
                .setProgress(all, done, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void showCompletedNotification() {
        notificationBuilder.setContentText(getString(R.string.sending_completed))
                .setProgress(0, 0, false)
                .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createForegroundNotification() {
        Intent notificationIntent = new Intent(this, top.yztz.msggo.activities.SendingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.preparing_to_send))
                .setSmallIcon(R.drawable.ic_send_small)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Message Send Channel", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Channel for message send progress");
        notificationManager.createNotificationChannel(channel);
    }

    private void registerSmsReceiver() {
        IntentFilter filter = new IntentFilter(SMSSender.SENT_SMS_ACTION);
        ContextCompat.registerReceiver(this, smsStatusReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterSmsReceiver() {
        try {
            unregisterReceiver(smsStatusReceiver);
        } catch (Exception e) {
            // Ignore if not registered
        }
    }
}

