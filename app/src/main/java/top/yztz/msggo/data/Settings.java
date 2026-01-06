package top.yztz.msggo.data;

public class Settings {
    // edit after import
    public static final boolean EDIT_AFTER_IMPORT_DEFAULT = false;
    // Send delay (ms)
    public static final int SEND_DELAY_DEFAULT = 3000;
    public static final int SEND_DELAY_MIN = 1000;
    public static final int SEND_DELAY_MAX = 8000;
    public static final int SEND_DELAY_STEP_UNIT = 500;
    public static final boolean SEND_DELAY_RANDOMIZATION_DEFAULT = true;
    // SMS rate
    public static final float SMS_RATE_DEFAULT = 0.1f;
    public static final float SMS_RATE_MIN = 0.0f;
    public static final float SMS_RATE_MAX = 10.0f;
    // privacy and disclaimer
    public static final boolean PRIVACY_ACCEPTED_DEFAULT = false;
    public static final boolean DISCLAIMER_ACCEPTED_DEFAULT = false;
    // language
    public static final String LANGUAGE_DEFAULT = "auto";
}
