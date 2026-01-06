package top.yztz.msggo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

public class SettingManager {
    private static final String TAG = "SettingManager";
    private static final String PREF_NAME = "setting_prefs";
    private static SharedPreferences mSp;
    private static SharedPreferences.Editor mEditor;
    private static final HashMap<String, Object> DefaultPropMap = new HashMap<>();

    private static final String SEND_DELAY_KEY = "send_delay_v1";
    private static final String SEND_DELAY_RANDOMIZATION_KEY = "send_delay_randomization_v1";
    private static final String EDIT_AFTER_IMPORT_KEY = "edit_after_import_v1";
    private static final String SMS_RATE_KEY = "sms_rate_v1";
    private static final String LANGUAGE_KEY = "language_v1";
    private static final String PRIVACY_ACCEPTED_KEY = "privacy_accepted";
    private static final String DISCLAIMER_ACCEPTED_KEY = "disclaimer_accepted";


    // Default values
    static {
        DefaultPropMap.put(SEND_DELAY_KEY, Settings.SEND_DELAY_DEFAULT);
        DefaultPropMap.put(SEND_DELAY_RANDOMIZATION_KEY, Settings.SEND_DELAY_RANDOMIZATION_DEFAULT);
        DefaultPropMap.put(EDIT_AFTER_IMPORT_KEY, Settings.EDIT_AFTER_IMPORT_DEFAULT);
        DefaultPropMap.put(SMS_RATE_KEY, Settings.SMS_RATE_DEFAULT);
        DefaultPropMap.put(LANGUAGE_KEY, Settings.LANGUAGE_DEFAULT);
        DefaultPropMap.put(PRIVACY_ACCEPTED_KEY, Settings.PRIVACY_ACCEPTED_DEFAULT);
        DefaultPropMap.put(DISCLAIMER_ACCEPTED_KEY, Settings.DISCLAIMER_ACCEPTED_DEFAULT);
    }

    public static void init(Context context) {
        mSp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSp.edit();
        Log.i(TAG, "Initializing SettingManager");
        for (String key : DefaultPropMap.keySet()) {
            //不包含key时使用默认值初始化
            if (!mSp.contains(key)) {
                Object obj = DefaultPropMap.get(key);
                Log.d(TAG, "Setting default value for: " + key + " -> " + obj);
                if (obj instanceof Integer) mEditor.putInt(key, (Integer) obj);
                else if (obj instanceof String) mEditor.putString(key, (String) obj);
                else if (obj instanceof Boolean) mEditor.putBoolean(key, (Boolean) obj);
            }
        }
        mEditor.apply();
    }


    public static boolean autoEnterEditor() {
        return mSp.getBoolean(EDIT_AFTER_IMPORT_KEY, Settings.EDIT_AFTER_IMPORT_DEFAULT);
    }

    public static void setAutoEnterEditor(boolean flag) {
        mEditor.putBoolean(EDIT_AFTER_IMPORT_KEY, flag);
        mEditor.apply();
    }

    public static int getDelay() {
        return mSp.getInt(SEND_DELAY_KEY, Settings.SEND_DELAY_DEFAULT);
    }

    public static void setDelay(int num) {
        mEditor.putInt(SEND_DELAY_KEY, num);
        mEditor.apply();
    }

    public static float getSmsRate() {
        return mSp.getFloat(SMS_RATE_KEY, Settings.SMS_RATE_DEFAULT);
    }

    public static void setSmsRate(float rate) {
        mEditor.putFloat(SMS_RATE_KEY, rate).apply();
    }

    public static boolean isPrivacyAccepted() {
        return mSp.getBoolean(PRIVACY_ACCEPTED_KEY, Settings.PRIVACY_ACCEPTED_DEFAULT);
    }

    public static void setPrivacyAccepted(boolean flag) {
        mEditor.putBoolean(PRIVACY_ACCEPTED_KEY, flag).apply();
    }

    public static boolean isDisclaimerAccepted() {
        return mSp.getBoolean(DISCLAIMER_ACCEPTED_KEY, Settings.DISCLAIMER_ACCEPTED_DEFAULT);
    }

    public static void setDisclaimerAccepted(boolean flag) {
        mEditor.putBoolean(DISCLAIMER_ACCEPTED_KEY, flag).apply();
    }

    public static String getLanguage() {
        return mSp.getString(LANGUAGE_KEY, Settings.LANGUAGE_DEFAULT);
    }

    public static void setLanguage(String lang) {
        mEditor.putString(LANGUAGE_KEY, lang).apply();
    }

    public static boolean isRandomizeDelay() {
        return mSp.getBoolean(SEND_DELAY_RANDOMIZATION_KEY, Settings.SEND_DELAY_RANDOMIZATION_DEFAULT);
    }

    public static void setRandomizeDelay(boolean flag) {
        mEditor.putBoolean(SEND_DELAY_RANDOMIZATION_KEY, flag).apply();
    }

}
