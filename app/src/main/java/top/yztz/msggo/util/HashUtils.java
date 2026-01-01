package top.yztz.msggo.util;

import android.text.TextUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    /**
     * 将任意字符串转为 MD5
     */
    public static String toMd5(String input) {
        if (TextUtils.isEmpty(input)) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return String.valueOf(input.hashCode()); // 兜底方案
        }
    }
}