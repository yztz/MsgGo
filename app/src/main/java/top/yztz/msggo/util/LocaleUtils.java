package top.yztz.msggo.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.ArrayList;
import java.util.List;

import top.yztz.msggo.R;
import top.yztz.msggo.data.SettingManager;

public class LocaleUtils {

    public static void applyLocale() {
        String lang = SettingManager.getLanguage();
        if ("auto".equals(lang)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
        }
    }

    public static void setLocale(String lang) {
        SettingManager.setLanguage(lang);
        if ("auto".equals(lang)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
        }
    }

    public static String[] getSupportedLanguages(Context context) {
        return context.getResources().getStringArray(R.array.supported_languages);
    }
}
