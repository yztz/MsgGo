package top.yzzblog.messagehelper.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.yzzblog.messagehelper.util.FileUtil;

public class HistoryManager {
    private static final String PREF_NAME = "history_prefs";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_SIZE = 6;


    public static void addHistory(Context context, DataContext dataContext) {
        String path = dataContext.path;
        if (TextUtils.isEmpty(dataContext.path)) return;

        dataContext.timestamp = System.currentTimeMillis();

        List<DataContext> list = getHistory(context);

        // Remove existing item to move to top
        for (int i = 0; i < list.size(); i++) {
            if (path.equals(list.get(i).path)) {
                list.remove(i);
                break;
            }
        }

        // Add new item to top
        list.add(0, dataContext);

        // Trim size
        if (list.size() > MAX_HISTORY_SIZE) {
            list = list.subList(0, MAX_HISTORY_SIZE);
        }

        saveHistory(context, list);
    }

    public static void addHistory(Context context, String path, String template, String numberColumn, String signature) {
        if (TextUtils.isEmpty(path)) return;

        List<DataContext> list = getHistory(context);
        
        // Remove existing item to move to top
        for (int i = 0; i < list.size(); i++) {
            if (path.equals(list.get(i).path)) {
                list.remove(i);
                break;
            }
        }
        
        // Add new item to top
        list.add(0, new DataContext(path, System.currentTimeMillis(), template, numberColumn, signature));
        
        // Trim size
        if (list.size() > MAX_HISTORY_SIZE) {
            list = list.subList(0, MAX_HISTORY_SIZE);
        }
        
        saveHistory(context, list);
    }
    
    public static DataContext getItem(Context context, String path) {
        if (TextUtils.isEmpty(path)) return null;
        for (DataContext item : getHistory(context)) {
            if (path.equals(item.path)) {
                return item;
            }
        }
        return null;
    }

    public static List<DataContext> getHistory(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = sp.getString(KEY_HISTORY, "");
        List<DataContext> list = new ArrayList<>();
        
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONArray jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    list.add(new DataContext(
                            obj.optString("path"),
                            obj.optLong("timestamp"),
                            obj.optString("template"),
                            obj.optString("numberColumn"),
                            obj.optString("signature")
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void clearHistory(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_HISTORY).apply();
    }

    private static void saveHistory(Context context, List<DataContext> list) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (DataContext item : list) {
                JSONObject obj = new JSONObject();
                obj.put("path", item.path);
                obj.put("timestamp", item.timestamp);
                obj.put("template", item.template);
                obj.put("numberColumn", item.numberColumn);
                obj.put("signature", item.signature);
                jsonArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HISTORY, jsonArray.toString()).apply();
    }
    

}
