package top.yzzblog.messagehelper.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import java.util.ArrayList;

public class SMSSender {
    public static final String SENT_SMS_ACTION = "top.yzzblog.messagehelper.action.SENT_SMS_ACTION";

    private static final SmsManager manager = SmsManager.getDefault();

    /**
     * 发送短信
     *
     * @param context
     * @param content 发送内容
     * @param phoneNumber 电话号码
     * @param code 独一无二的请求码（用以广播接收）
     */
    public static void sendMessage(Context context, String content, String phoneNumber, int code) {
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra("code", code);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, code, sentIntent, PendingIntent.FLAG_ONE_SHOT);

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
