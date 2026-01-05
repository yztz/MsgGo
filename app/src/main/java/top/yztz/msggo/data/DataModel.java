package top.yztz.msggo.data;


import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import top.yztz.msggo.exception.DataLoadFailed;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.ExcelReader;
import top.yztz.msggo.util.HashUtils;

public class DataModel implements Serializable {
    private static String[] titles = null;
    private static String path;
    private static long timestamp;
    private static String template;
    private static String numberColumn;
    private static String signature;
    private static int subId;
    private static List<HashMap<String, String >> data = null;

    private static boolean loaded = false;

    public static boolean loaded() {
        return loaded;
    }

    /**
     * Must be called in the non-UI thread
     *
     * @param context context
     * @param path    excel file to load
     */
    public static synchronized void load(Context context, String path) throws DataLoadFailed {
        ExcelReader reader = new ExcelReader();
        reader.read(context, path);
        DataModel.data = reader.readExcelContent();
        DataModel.titles = reader.getTitles();
        DataModel.path = path;
        DataModel.signature = HashUtils.toMd5(path + "+" + String.join("-", reader.getTitles()));
        DataModel.timestamp = System.currentTimeMillis();

        DataModel.template = "";
        DataModel.numberColumn = "";
        DataModel.subId = SMSSender.getDefaultSubID();
        loaded = true;
    }

    public static void saveAsHistory(Context context) {
        assert loaded();
        HistoryManager.addHistory(context, path, template, subId,numberColumn, signature);
    }

    public static int getRowCount() {
        return data.size();
    }

    public static HashMap<String, String> getRow(int index) {
        return data.get(index);
    }

    public static String[] getTitles() {
        return titles;
    }

    public static String getPath() {
        return path;
    }

    public static long getTimestamp() {
        return timestamp;
    }

    public static String getTemplate() {
        return template;
    }

    public static void setTemplate(String template) {
        DataModel.template = template;
    }

    public static String getNumberColumn() {
        return numberColumn;
    }

    public static void setNumberColumn(String numberColumn) {
        DataModel.numberColumn = numberColumn;
    }

    public static String getSignature() {
        return signature;
    }


    public static int getSubId() {
        return subId;
    }

    public static void setSubId(int subId) {
        DataModel.subId = subId;
    }

    public static void clear() {
        data = null;
        titles = null;
        loaded = false;
    }
}
