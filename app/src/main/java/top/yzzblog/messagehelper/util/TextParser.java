package top.yzzblog.messagehelper.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    public static String parse(String content, Map<String, String> data) {

        Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher m = p.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String value = data.get(key);
            //如果不存在映射关系，则替换为"null"
            m.appendReplacement(sb, value == null ? "null" : value);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
