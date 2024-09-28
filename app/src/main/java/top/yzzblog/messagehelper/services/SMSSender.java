package top.yzzblog.messagehelper.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.util.ToastUtil;

public class SMSSender {
    public static final String SENT_SMS_ACTION = "top.yzzblog.messagehelper.action.SENT_SMS_ACTION";


    public static SubscriptionInfo getSubBySlotId(Context context, int slot) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        @SuppressLint("MissingPermission") List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo sub : subscriptionInfoList) {
            if (sub.getSimSlotIndex() == slot) return sub;
        }


//        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
//            int subscriptionId = subscriptionInfo.getSubscriptionId();
//            String carrierName = subscriptionInfo.getCarrierName().toString();
//            String displayName = subscriptionInfo.getDisplayName().toString();
//            // 获取更多的SIM卡信息，如手机号、SIM槽位等
//        }
        return null;
    }


    public static int getDefaultSubID() {
        return SmsManager.getDefaultSmsSubscriptionId();
    }


    public static List<SubscriptionInfo> getSubs(Context context) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        @SuppressLint("MissingPermission") List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

//        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
//            int subscriptionId = subscriptionInfo.getSubscriptionId();
//            String carrierName = subscriptionInfo.getCarrierName().toString();
//            String displayName = subscriptionInfo.getDisplayName().toString();
//            // 获取更多的SIM卡信息，如手机号、SIM槽位等
//        }
        return subscriptionInfoList;
    }
    /**
     * 发送短信
     *
     * @param context
     * @param content 发送内容
     * @param phoneNumber 电话号码
     * @param code 独一无二的请求码（用以广播接收）
     */
    public static void sendMessage(Context context, String content, String phoneNumber, int subId, int code) {
        final SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra("code", code);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, code, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        if (content.length() > 70) {
            ArrayList<String> msgs = manager.divideMessage(content);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();

            for (int i = 0; i < msgs.size(); i++) {
                sentIntents.add(sentPI);
            }
            manager.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
        } else {
            manager.sendTextMessage(phoneNumber, null, content, sentPI, null);
        }

    }
}
