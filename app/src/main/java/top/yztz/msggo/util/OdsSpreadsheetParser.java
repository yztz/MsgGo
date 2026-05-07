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

import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.exception.DataLoadFailed;

class OdsSpreadsheetParser implements SpreadsheetParser {
    private static final String TAG = "OdsSpreadsheetParser";

    private int colNum;
    private String[] titles;
    private List<List<String>> rows; // includes header at index 0

    @Override
    public boolean supports(String extension) {
        return ".ods".equals(extension);
    }

    @Override
    public void parse(String path) throws DataLoadFailed {
        rows = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("content.xml".equals(entry.getName())) {
                    parseXml(zis);
                    break;
                }
            }
        } catch (IOException e) {
            throw new DataLoadFailed(e);
        }

        if (rows.isEmpty() || rows.get(0).isEmpty())
            throw new DataLoadFailed(R.string.error_no_header);

        List<String> header = rows.get(0);
        colNum = header.size();
        titles = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            titles[i] = header.get(i);
            if (TextUtils.isEmpty(titles[i])) throw new DataLoadFailed(R.string.error_empty_title_column);
        }

        int dataRowCount = rows.size() - 1;
        if (dataRowCount > Settings.EXCEL_ROW_COUNT_MAX) throw new DataLoadFailed(R.string.file_too_much_row);
        if (dataRowCount <= 0) throw new DataLoadFailed(R.string.error_empty_content);
        Log.i(TAG, String.format("rows=%d, cols=%d", dataRowCount, colNum));
    }

    private void parseXml(InputStream is) throws IOException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(is, "UTF-8");

            boolean inTable = false;
            boolean inRow = false;
            boolean inCell = false;
            boolean inTextP = false;
            List<String> currentRow = null;
            String cellText = null;
            String officeValue = null;
            int colsRepeated = 1;
            int rowsRepeated = 1;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String tag = parser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if ("table:table".equals(tag)) {
                        inTable = true;
                    } else if (inTable && "table:table-row".equals(tag)) {
                        inRow = true;
                        currentRow = new ArrayList<>();
                        String rep = parser.getAttributeValue(null, "table:number-rows-repeated");
                        rowsRepeated = rep != null ? Integer.parseInt(rep) : 1;
                    } else if (inRow && ("table:table-cell".equals(tag) || "table:covered-table-cell".equals(tag))) {
                        inCell = true;
                        cellText = "";
                        officeValue = parser.getAttributeValue(null, "office:value");
                        if (officeValue == null)
                            officeValue = parser.getAttributeValue(null, "office:date-value");
                        if (officeValue == null)
                            officeValue = parser.getAttributeValue(null, "office:boolean-value");
                        String rep = parser.getAttributeValue(null, "table:number-columns-repeated");
                        colsRepeated = rep != null ? Integer.parseInt(rep) : 1;
                    } else if (inCell && "text:p".equals(tag)) {
                        inTextP = true;
                    }
                } else if (event == XmlPullParser.TEXT) {
                    if (inTextP) cellText += parser.getText();
                } else if (event == XmlPullParser.END_TAG) {
                    if ("table:table".equals(tag)) {
                        break; // only read the first sheet
                    } else if ("table:table-row".equals(tag)) {
                        inRow = false;
                        if (currentRow != null) {
                            // trim trailing empty cells added by ODS for virtual padding
                            int last = currentRow.size() - 1;
                            while (last >= 0 && currentRow.get(last).isEmpty()) last--;
                            if (last >= 0) {
                                List<String> trimmed = new ArrayList<>(currentRow.subList(0, last + 1));
                                int addCount = Math.min(rowsRepeated, Settings.EXCEL_ROW_COUNT_MAX + 1);
                                for (int i = 0; i < addCount; i++) rows.add(new ArrayList<>(trimmed));
                            }
                        }
                        rowsRepeated = 1;
                    } else if ("table:table-cell".equals(tag) || "table:covered-table-cell".equals(tag)) {
                        inCell = false;
                        inTextP = false;
                        String value = (cellText != null && !cellText.isEmpty())
                                ? cellText
                                : (officeValue != null ? officeValue : "");
                        value = value.trim();
                        for (int i = 0; i < colsRepeated; i++) currentRow.add(value);
                        colsRepeated = 1;
                    } else if ("text:p".equals(tag)) {
                        inTextP = false;
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new IOException("ODS parse error: " + e.getMessage(), e);
        }
    }

    @Override
    public String[] getTitles() {
        return titles;
    }

    @Override
    public ArrayList<HashMap<String, String>> getContent() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            HashMap<String, String> content = new HashMap<>();
            for (int j = 0; j < colNum; j++) {
                content.put(titles[j], j < row.size() ? row.get(j).trim() : "");
            }
            list.add(content);
        }
        return list;
    }
}
