package top.yztz.msggo.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast = null;

    public static void show(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        toast.show();
}
}
