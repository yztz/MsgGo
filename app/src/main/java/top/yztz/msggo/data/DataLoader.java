package top.yztz.msggo.data;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
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
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import top.yztz.msggo.activities.MainActivity;
import top.yztz.msggo.exception.DataLoadFailed;
import top.yztz.msggo.services.LoadService;
import top.yztz.msggo.services.SMSSender;
import top.yztz.msggo.util.FileUtil;
import top.yztz.msggo.util.HashUtils;


public class DataLoader {
    private static final String TAG = "dataLoader";
    private static DataModel dataModel = null;
    private static SpManager spManager;

    private static DataContext dataContext = new DataContext();

    //默认属性初始化表
    public static final HashMap<String, Object> DefaultPropMap = new HashMap<>();

    static {
        //消息内容
//        DefaultPropMap.put("content", "");
        //发送间隔
        DefaultPropMap.put("send_delay", 3000);
        //加载完成是否自动进入编辑界面
        DefaultPropMap.put("auto_enter_editor", false);
        // sim 卡插槽
        DefaultPropMap.put("sub_id", SMSSender.getDefaultSubID());
        // 短信资费
        DefaultPropMap.put("sms_rate", "0.1");
        // 是否同意隐私协议
        DefaultPropMap.put("privacy_accepted", false);
        // 是否同意免责声明
        DefaultPropMap.put("disclaimer_accepted", false);

    }

    public static String getContent() {
//        return spManager.mSp.getString("content", "");
        return dataContext.template;
    }

    public static void setContent(String content) {
//        spManager.mEditor.putString("content", content).apply();
//        spManager.mEditor.apply();
        dataContext.template = content;
    }

    public static int getSimSubId() {
        return spManager.mSp.getInt("sub_id", 0);
    }

    public static void setSimSubId(int id) {
        spManager.mEditor.putInt("sub_id", id);
        spManager.mEditor.apply();
    }


    public static int getDelay() {
        return spManager.mSp.getInt("send_delay", 5000);
    }

    public static void setDelay(int num) {
        spManager.mEditor.putInt("send_delay", num);
        spManager.mEditor.apply();
    }

    public static String getLastPath() {
        return dataContext.path;
    }

    public static String getLastSignature() {
        return dataContext.signature;
    }

    public static DataContext getDataContext() {
        return dataContext;
    }

    public static void setLastPath(String path) {
        dataContext = new DataContext();
        dataContext.path = path;
        dataContext.signature = calculateSignature();
    }

    public static boolean autoEnterEditor() {
        return spManager.mSp.getBoolean("auto_enter_editor", false);
    }

    public static void setAutoEnterEditor(boolean flag) {
        spManager.mEditor.putBoolean("auto_enter_editor", flag);
        spManager.mEditor.apply();
    }


    /**
     * 加载excel
     *
     * @param path 文件路径
     */
    public static void load(String path, Context context) {
        Log.d("msgD", "开始加载path: " + path);

        Intent intent = new Intent(context, LoadService.class);
        intent.putExtra("path", path);
        context.startService(intent);
    }

    public static void __load(String path) throws DataLoadFailed {
        ExcelReader.read(path);
        dataModel = new DataModel(ExcelReader.readExcelContent());
    }


    public static DataModel getDataModel() {
        return dataModel;
    }

    public static String[] getTitles() {
        if (dataModel == null) return null;
        return ExcelReader.titles;
    }

    private static String calculateSignature() {
        if (TextUtils.isEmpty(getLastPath()) || dataModel == null || ExcelReader.titles == null || ExcelReader.titles.length == 0) {
            return "";
        }
        String input = getLastPath() + "+" + String.join("-", getTitles());
        return HashUtils.toMd5(input);
    }

    /**
     * 必须被第一个调用
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        //初始化SPManager
        spManager = new SpManager(context);
        //初始化属性表
        for (String key : DefaultPropMap.keySet()) {
            //不包含key时使用默认值初始化
            if (!spManager.mSp.contains(key)) {
                Object obj = DefaultPropMap.get(key);
                if (obj instanceof Integer) spManager.mEditor.putInt(key, (Integer) obj);
                else if (obj instanceof String) spManager.mEditor.putString(key, (String) obj);
                else if (obj instanceof Boolean) spManager.mEditor.putBoolean(key, (Boolean) obj);
            }
        }
        spManager.mEditor.apply();

//        Intent intent = ((MainActivity) context).getIntent();
//        String action = intent.getAction();
//        Uri uri = null;
//
//        if (Intent.ACTION_VIEW.equals(action)) {
//            uri = intent.getData();
//        } else if (Intent.ACTION_SEND.equals(action)) {
//            uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri.class);
//        }
//
//        if (uri != null) {
//            load(FileUtil.getFilePathFromContentUri(context, uri), context);
//        }
    }


    public static String getNumberColumn() {
        return dataContext.numberColumn;
    }

    public static void setNumberColumn(String col) {
        dataContext.numberColumn = col;
    }

    public static String getSmsRate() {
        return spManager.mSp.getString("sms_rate", "0.1");
    }

    public static void setSmsRate(String rate) {
        spManager.mEditor.putString("sms_rate", rate).apply();
    }

    public static boolean isPrivacyAccepted() {
        return spManager.mSp.getBoolean("privacy_accepted", false);
    }

    public static void setPrivacyAccepted(boolean flag) {
        spManager.mEditor.putBoolean("privacy_accepted", flag).apply();
    }

    public static boolean isDisclaimerAccepted() {
        return spManager.mSp.getBoolean("disclaimer_accepted", false);
    }

    public static void setDisclaimerAccepted(boolean flag) {
        spManager.mEditor.putBoolean("disclaimer_accepted", flag).apply();
    }

    public static void clear() {
        dataModel = null;
        dataContext = new DataContext();
    }


}

class SpManager {

    public final SharedPreferences mSp;
    public final SharedPreferences.Editor mEditor;

    SpManager(Context context) {
        mSp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        mEditor = mSp.edit();
    }

}


class ExcelReader {
    private static final String TAG = "excelReader";
    private static Workbook wb;
    private static Sheet sheet;
    public static int colNum = 0;
//    public static int rowNum = 0;
    public static String[] titles = null;
    public static int[] titleColumns = null;


    static void read(String path) throws DataLoadFailed {
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
            if (firstRow == null) throw new DataLoadFailed("未找到首行标题（内容为空）");
            colNum = firstRow.getPhysicalNumberOfCells();

            if (firstRow.getLastCellNum() - firstRow.getFirstCellNum() != colNum) throw new DataLoadFailed("数据内容列非连续");
            //获取标题
            readExcelTitle();
            //得到总行数（不包含标题）
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum == -1 || lastRowNum == 0) throw new DataLoadFailed("内容为空");
            Log.i(TAG, String.format("lastRowNum=%d, colNum=%d(%d-%d)", lastRowNum, colNum, firstRow.getFirstCellNum(), firstRow.getLastCellNum() - 1));
        } catch (IOException e) {
            //抛出读取异常
            e.printStackTrace();
            throw new DataLoadFailed();
        }
    }


    private static void readExcelTitle() throws DataLoadFailed {
        Row row = sheet.getRow(0);
        titles = new String[colNum];
        titleColumns = new int[colNum];
        int startCol = row.getFirstCellNum();
        for (int i = 0; i < titles.length; i++) {
            titles[i] = getCellFormatValue(row.getCell(startCol + i));
            titleColumns[i] = startCol + i;
            if (TextUtils.isEmpty(titles[i])) throw new DataLoadFailed("标题内容列(" + (startCol + i) +")为空");
        }
    }

    public static ArrayList<HashMap<String, String>> readExcelContent() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> content = null;
        Row currentRow;
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            currentRow = sheet.getRow(i);
            content = new HashMap<>();
            for (int j = 0; j < titleColumns.length; j++) {
                content.put(titles[j], getCellFormatValue(currentRow.getCell(titleColumns[j])).trim());
            }
            list.add(content);
        }
        return list;
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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
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
