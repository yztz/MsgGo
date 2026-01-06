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

package top.yztz.msggo.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

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

    public static String getLanguageDisplayName(Context context, String langCode) {
        if ("auto".equalsIgnoreCase(langCode)) {
            return context.getString(R.string.language_auto);
        }
        Locale locale = Locale.forLanguageTag(langCode);
        return locale.getDisplayLanguage(locale);
    }
}
