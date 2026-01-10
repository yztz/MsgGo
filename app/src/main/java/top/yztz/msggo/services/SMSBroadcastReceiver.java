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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import top.yztz.msggo.BuildConfig;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSBroadcastReceiver";
    private final Callback callback;
    private final Map<Integer, SmsStatus> smsStatusMap = new HashMap<>();

    // 短信状态追踪类
    private static class SmsStatus {
        int totalParts;
        int successParts;
        int failedParts;
        String phone;

        boolean isComplete() {
            return (successParts + failedParts) >= totalParts;
        }

        boolean isAllSuccess() {
            return successParts == totalParts && failedParts == 0;
        }
    }

    public interface Callback {
        void onSent(int code, boolean success);
    }

    public SMSBroadcastReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int code = intent.getIntExtra("code", -1);
        String phone = intent.getStringExtra("phone");
        int part = intent.getIntExtra("part", -1);
        int totalParts = intent.getIntExtra("totalParts", 1);
        int resultCode = getResultCode();
        boolean success = (resultCode == Activity.RESULT_OK);

        // 获取或创建状态追踪对象
        SmsStatus status = smsStatusMap.get(code);
        if (status == null) {
            status = new SmsStatus();
            status.totalParts = totalParts;
            status.phone = phone;
            smsStatusMap.put(code, status);
        }

        // 更新统计
        if (success) {
            status.successParts++;
        } else {
            status.failedParts++;
        }

        // 记录日志
        String log = buildLogMessage(code, phone, part, totalParts, success, resultCode);
        if (success) {
            Log.i(TAG, log);
        } else {
            Log.w(TAG, log);
        }

        // 检查是否所有分片都已确认
        if (status.isComplete()) {
            boolean allSuccess = status.isAllSuccess();

            // 记录最终状态
            String finalLog = String.format(
                    "SMS sending completed for code=%d, phone=%s, success=%d/%d",
                    code, maskPhone(phone), status.successParts, status.totalParts
            );
            Log.i(TAG, finalLog);

            // 回调通知（只回调一次）
            if (callback != null) {
                callback.onSent(code, allSuccess);
            }

            // 清理状态
            smsStatusMap.remove(code);
        }
    }

    // 构建日志消息
    private String buildLogMessage(int code, String phone, int part,
                                   int totalParts, boolean success, int resultCode) {
        String maskedPhone = phone != null ? maskPhone(phone) : "Unknown";

        if (totalParts > 1) {
            // 多条短信
            if (success) {
                return String.format("SMS part %d/%d sent successfully (code=%d, phone=%s)",
                        part + 1, totalParts, code, maskedPhone);
            } else {
                return String.format("SMS part %d/%d failed (code=%d, phone=%s, error=%s[%d])",
                        part + 1, totalParts, code, maskedPhone,
                        getSmsErrorMessage(resultCode), resultCode);
            }
        } else {
            // 单条短信
            if (success) {
                return String.format("SMS sent successfully (code=%d, phone=%s)",
                        code, maskedPhone);
            } else {
                return String.format("SMS failed (code=%d, phone=%s, error=%s[%d])",
                        code, maskedPhone, getSmsErrorMessage(resultCode), resultCode);
            }
        }
    }

    // 电话号码脱敏
    private String maskPhone(String phone) {
        if(BuildConfig.DEBUG) {
            return phone;
        }
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        if (phone.length() <= 7) {
            return phone.substring(0, 2) + "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }

    // 获取详细的错误信息
    private String getSmsErrorMessage(int resultCode) {
        return switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure";
            case SmsManager.RESULT_ERROR_NO_SERVICE -> "No service";
            case SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU";
            case SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off";
            case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "Limit exceeded";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED -> "Short code not allowed";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED -> "Short code never allowed";
            case Activity.RESULT_CANCELED -> "Canceled";
            default -> "Unknown error";
        };
    }
}
