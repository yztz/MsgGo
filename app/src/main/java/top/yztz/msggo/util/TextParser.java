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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public static String parse(String content, Map<String, String> data) {
        Matcher m = VARIABLE_PATTERN.matcher(content);

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
