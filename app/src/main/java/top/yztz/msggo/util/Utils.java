package top.yztz.msggo.util;

import java.util.List;

public class Utils {

    public static int[] toArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        int i = 0;
        for (int tmp : list) {
            arr[i++] = tmp;
        }
        return arr;
    }
}
