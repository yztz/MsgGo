package top.yztz.msggo.data;

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

import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.FileUtil;

public class HistoryManager {
    private static final String PREF_NAME = "history_prefs";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_SIZE = 6;

    public static class HistoryItem {
        public String path;
        public String template;
        public String numberColumn;
        public int subId;
        public String signature;
        public long timestamp;

        public HistoryItem(String path, long timestamp, String template, int subId, String numberColumn, String signature) {
            this.path = path;
            this.timestamp = timestamp;
            this.template = template;
            this.subId = subId;
            this.numberColumn = numberColumn;
            this.signature = signature;
        }

    }


    public static void addHistory(Context context, String path, String template, int subId, String numberColumn, String signature) {
        if (TextUtils.isEmpty(path)) return;

        List<HistoryItem> list = getHistory(context);
        
        // Remove existing item to move to top
        for (int i = 0; i < list.size(); i++) {
            if (path.equals(list.get(i).path)) {
                list.remove(i);
                break;
            }
        }
        
        // Add new item to top
        list.add(0, new HistoryItem(path, System.currentTimeMillis(), template, subId, numberColumn, signature));
        
        // Trim size
        if (list.size() > MAX_HISTORY_SIZE) {
            list = list.subList(0, MAX_HISTORY_SIZE);
        }
        
        saveHistory(context, list);
    }

//    public static void addHistory(Context context, HistoryItem historyItem) {
//        if (TextUtils.isEmpty(historyItem.path)) return;
//
//        List<HistoryItem> list = getHistory(context);
//
//        // Remove existing item to move to top
//        for (int i = 0; i < list.size(); i++) {
//            if (historyItem.path.equals(list.get(i).path)) {
//                list.remove(i);
//                break;
//            }
//        }
//
//        // Add new item to top
//        list.add(0, historyItem);
//
//        // Trim size
//        if (list.size() > MAX_HISTORY_SIZE) {
//            list = list.subList(0, MAX_HISTORY_SIZE);
//        }
//
//        saveHistory(context, list);
//    }
    
    public static HistoryItem getItem(Context context, String path) {
        if (TextUtils.isEmpty(path)) return null;
        for (HistoryItem item : getHistory(context)) {
            if (path.equals(item.path)) {
                return item;
            }
        }
        return null;
    }

    public static List<HistoryItem> getHistory(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = sp.getString(KEY_HISTORY, "");
        List<HistoryItem> list = new ArrayList<>();
        
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONArray jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    list.add(new HistoryItem(
                            obj.optString("path"),
                            obj.optLong("timestamp"),
                            obj.optString("template"),
                            obj.optInt("subId", SMSSender.getDefaultSubID()),
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

    private static void saveHistory(Context context, List<HistoryItem> list) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (HistoryItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("path", item.path);
                obj.put("timestamp", item.timestamp);
                obj.put("template", item.template);
                obj.put("subId", item.subId);
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
