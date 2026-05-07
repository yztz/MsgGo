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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.exception.DataLoadFailed;

class PoiSpreadsheetParser implements SpreadsheetParser {
    private static final String TAG = "PoiSpreadsheetParser";
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private Sheet sheet;
    private int colNum;
    private String[] titles;
    private List<Integer> titleColumns;

    @Override
    public boolean supports(String extension) {
        return ".xls".equals(extension) || ".xlsx".equals(extension);
    }

    @Override
    public void parse(String path) throws DataLoadFailed {
        try (FileInputStream is = new FileInputStream(path)) {
            Workbook wb = path.endsWith(".xls")
                    ? new HSSFWorkbook(new POIFSFileSystem(is))
                    : new XSSFWorkbook(is);
            sheet = wb.getSheetAt(0);
            Row firstRow = sheet.getRow(0);
            if (firstRow == null) throw new DataLoadFailed(R.string.error_no_header);
            colNum = firstRow.getPhysicalNumberOfCells();
            if (firstRow.getLastCellNum() - firstRow.getFirstCellNum() != colNum)
                throw new DataLoadFailed(R.string.error_non_continuous_columns);
            readTitles();
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > Settings.EXCEL_ROW_COUNT_MAX) throw new DataLoadFailed(R.string.file_too_much_row);
            if (lastRowNum <= 0) throw new DataLoadFailed(R.string.error_empty_content);
            Log.i(TAG, String.format("rows=%d, cols=%d(%d-%d)",
                    lastRowNum, colNum, firstRow.getFirstCellNum(), firstRow.getLastCellNum() - 1));
        } catch (IOException e) {
            throw new DataLoadFailed(e);
        }
    }

    private void readTitles() throws DataLoadFailed {
        Row row = sheet.getRow(0);
        titles = new String[colNum];
        titleColumns = new ArrayList<>(colNum);
        int startCol = row.getFirstCellNum();
        for (int i = 0; i < colNum; i++) {
            titles[i] = cellToString(row.getCell(startCol + i));
            titleColumns.add(startCol + i);
            if (TextUtils.isEmpty(titles[i])) throw new DataLoadFailed(R.string.error_empty_title_column);
        }
    }

    @Override
    public String[] getTitles() {
        return titles;
    }

    @Override
    public ArrayList<HashMap<String, String>> getContent() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;
            HashMap<String, String> content = new HashMap<>();
            for (int j = 0; j < titleColumns.size(); j++) {
                content.put(titles[j], cellToString(row.getCell(titleColumns.get(j))));
            }
            list.add(content);
        }
        return list;
    }

    private boolean isRowEmpty(Row row) {
        for (int colIdx : titleColumns) {
            if (!TextUtils.isEmpty(cellToString(row.getCell(colIdx)))) return false;
        }
        return true;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }
}
