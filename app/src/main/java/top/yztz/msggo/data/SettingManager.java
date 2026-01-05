package top.yztz.msggo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;

import top.yztz.msggo.util.LocaleUtils;

public class SettingManager {
    private static final String TAG = "SettingManager";
    private static final String PREF_NAME = "setting_prefs";
    private static SharedPreferences mSp;
    private static SharedPreferences.Editor mEditor;
    private static final HashMap<String, Object> DefaultPropMap = new HashMap<>();

    public static void init(Context context) {
        mSp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSp.edit();
        for (String key : DefaultPropMap.keySet()) {
            //不包含key时使用默认值初始化
            if (!mSp.contains(key)) {
                Object obj = DefaultPropMap.get(key);
                if (obj instanceof Integer) mEditor.putInt(key, (Integer) obj);
                else if (obj instanceof String) mEditor.putString(key, (String) obj);
                else if (obj instanceof Boolean) mEditor.putBoolean(key, (Boolean) obj);
            }
        }
        mEditor.apply();
    }

    // Default values
    static {
        //发送间隔
        DefaultPropMap.put("send_delay", 3000);
        //加载完成是否自动进入编辑界面
        DefaultPropMap.put("auto_enter_editor", false);
        // 短信资费
        DefaultPropMap.put("sms_rate", "0.1");
        // 是否同意隐私协议
        DefaultPropMap.put("privacy_accepted", false);
        // 是否同意免责声明
        DefaultPropMap.put("disclaimer_accepted", false);
        // 语言设置 (auto, en, zh)
        DefaultPropMap.put("language", "auto");
    }

    public static boolean autoEnterEditor() {
        return mSp.getBoolean("auto_enter_editor", false);
    }

    public static void setAutoEnterEditor(boolean flag) {
        mEditor.putBoolean("auto_enter_editor", flag);
        mEditor.apply();
    }

    public static int getDelay() {
        return mSp.getInt("send_delay", 5000);
    }

    public static void setDelay(int num) {
        mEditor.putInt("send_delay", num);
        mEditor.apply();
    }

    public static String getSmsRate() {
        return mSp.getString("sms_rate", "0.1");
    }

    public static void setSmsRate(String rate) {
        mEditor.putString("sms_rate", rate).apply();
    }

    public static boolean isPrivacyAccepted() {
        return mSp.getBoolean("privacy_accepted", false);
    }

    public static void setPrivacyAccepted(boolean flag) {
        mEditor.putBoolean("privacy_accepted", flag).apply();
    }

    public static boolean isDisclaimerAccepted() {
        return mSp.getBoolean("disclaimer_accepted", false);
    }

    public static void setDisclaimerAccepted(boolean flag) {
        mEditor.putBoolean("disclaimer_accepted", flag).apply();
    }

    public static String getLanguage() {
        return mSp.getString("language", "auto");
    }

    public static void setLanguage(String lang) {
        mEditor.putString("language", lang).apply();
    }

}
