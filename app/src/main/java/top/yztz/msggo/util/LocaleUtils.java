package top.yztz.msggo.util;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import top.yztz.msggo.data.DataLoader;

public class LocaleUtils {

    public static void applyLocale(Context context) {
        String lang = DataLoader.getLanguage();
        if ("auto".equals(lang)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
        }
    }

    public static void setLocale(String lang) {
        DataLoader.setLanguage(lang);
        if ("auto".equals(lang)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
        }
    }
}
