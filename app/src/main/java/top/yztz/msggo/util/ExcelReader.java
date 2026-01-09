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


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.exception.DataLoadFailed;

public class ExcelReader {
    private static final String TAG = "excelReader";
    private Workbook wb;
    private Sheet sheet;
    private int colNum = 0;
//    public static int rowNum = 0;
    private String[] titles = null;
    private List<Integer> titleColumns = null;


    public void read(String path) throws DataLoadFailed {
        File file = new File(path);
        if (file.exists() && file.length() > Settings.EXCEL_FILE_SIZE_MAX)
            throw new DataLoadFailed(R.string.file_too_large);

        try (FileInputStream is = new FileInputStream(path)) {
            //创建工作簿
            String postfix = path.substring(path.lastIndexOf("."));
            if (postfix.equals(".xls")) {
                // 针对 2003 Excel 文件
                wb = new HSSFWorkbook(new POIFSFileSystem(is));
            } else {
                // 针对2007 Excel 文件
                wb = new XSSFWorkbook(is);
            }
            //获取第一张工作表（约定）
            sheet = wb.getSheetAt(0);
            //获取行的列数（可自定）
            Row firstRow = sheet.getRow(0);
            if (firstRow == null) throw new DataLoadFailed(R.string.error_no_header);
            colNum = firstRow.getPhysicalNumberOfCells();

            if (firstRow.getLastCellNum() - firstRow.getFirstCellNum() != colNum) throw new DataLoadFailed(R.string.error_non_continuous_columns);
            //获取标题
            readExcelTitle();
            //得到总行数（不包含标题）
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > Settings.EXCEL_ROW_COUNT_MAX) throw new DataLoadFailed(R.string.file_too_much_row);
            if (lastRowNum == -1 || lastRowNum == 0) throw new DataLoadFailed(R.string.error_empty_content);
            Log.i(TAG, String.format("lastRowNum=%d, colNum=%d(%d-%d)", lastRowNum, colNum, firstRow.getFirstCellNum(), firstRow.getLastCellNum() - 1));
        } catch (IOException e) {
            throw new DataLoadFailed(e);
        }
    }


    private void readExcelTitle() throws DataLoadFailed {
        Row row = sheet.getRow(0);
        titles = new String[colNum];
        titleColumns = new ArrayList<>(colNum);
        int startCol = row.getFirstCellNum();
        for (int i = 0; i < titles.length; i++) {
            titles[i] = (getCellFormatValue(row.getCell(startCol + i)));
            titleColumns.add(startCol + i);
            if (TextUtils.isEmpty(titles[i])) throw new DataLoadFailed(R.string.error_empty_title_column);
        }
    }

    public ArrayList<HashMap<String, String>> readExcelContent() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> content = null;
        Row currentRow;
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            currentRow = sheet.getRow(i);
            content = new HashMap<>();
            for (int j = 0; j < titleColumns.size(); j++) {
                content.put(titles[j],
                        getCellFormatValue(currentRow.getCell(titleColumns.get(j))).trim());
            }
            list.add(content);
        }
        return list;
    }

    public String[] getTitles() {
        return titles;
    }

    /**
     * 将cell数据转化为字符串格式
     *
     * @param cell 单元格
     * @return 数据的字符串形式
     */
    private static String getCellFormatValue(Cell cell) {
        //判断是否为null或空串
        if (cell == null || cell.toString().trim().isEmpty()) {
            return "";
        }
        String cellValue = "";
        CellType cellType = cell.getCellType();
//        if (cellType == Cell.CELL_TYPE_FORMULA) { //表达式类型
//            cellType = evaluator.evaluate(cell).getCellType();
//        }

        switch (cellType) {
            case STRING: //字符串类型
                cellValue = cell.getStringCellValue().trim();
                cellValue = TextUtils.isEmpty(cellValue) ? "" : cellValue;
                break;
            case BOOLEAN:  //布尔类型
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case NUMERIC: //数值类型
                if (DateUtil.isCellDateFormatted(cell)) {  //判断日期类型
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    cellValue = sdf.format(cell.getDateCellValue());
                } else {  //否
                    cellValue = new DecimalFormat("#").format(cell.getNumericCellValue());
                }
                break;
            default: //其它类型，取空串吧
                cellValue = "";
                break;
        }
        return cellValue;
    }
}
