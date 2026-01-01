package top.yzzblog.messagehelper.data;

import android.text.TextUtils;

public class DataContext {
    public String path;
    public long timestamp;
    public String template;
    public String numberColumn;
    public String signature;

    public DataContext() {}
    public DataContext(String path, long timestamp, String template, String numberColumn, String signature) {
        this.path = path;
        this.timestamp = timestamp;
        this.template = template;
        this.numberColumn = numberColumn;
        this.signature = signature;
    }

    public String getFileName() {
        if (TextUtils.isEmpty(path)) return "";
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }
}
