package top.yzzblog.messagehelper.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;

import top.yzzblog.messagehelper.data.Message;

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static String getFilePathFromContentUri(Context context, Uri contentUri) {
        String fileName = getFileName(context, contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(context.getCacheDir() + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    private static String getFileName(Context context, Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get filename from content URI", e);
            }
        }
        if (fileName == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                }
            }
        }
        return fileName;
    }

    private static void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = Files.newOutputStream(dstFile.toPath());
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }


    public static String saveMessageArrayToFile(Context context, Message[] messages) {
        String fileName = "messages_" + System.currentTimeMillis() + ".ser";
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(messages);  // 序列化Message数组
            Log.d(TAG, "Message array has been serialized to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fileName;  // 返回生成的文件名
    }

    public static Message[] readMessageArrayFromFile(Context context, String fileName) {
        Message[] messages = null;
        try (FileInputStream fis = context.openFileInput(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            messages = (Message[]) ois.readObject();  // 反序列化Message数组
            Log.d(TAG, "Message array has been deserialized from file: " + fileName);

            // 成功读取后删除文件
//            boolean isDeleted = context.deleteFile(fileName);
//            if (isDeleted) {
//                Log.d(TAG, "File " + fileName + " has been deleted.");
//            } else {
//                Log.d(TAG, "Failed to delete file: " + fileName);
//            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return messages;
    }


    public static String getFileSignature(String path) {
        if (TextUtils.isEmpty(path)) return "";
        File file = new File(path);
        if (!file.exists() || !file.isFile()) return "";

        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);

            byte[] buffer = new byte[16384];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }

            // 将字节数组转换为十六进制字符串
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // 如果计算失败，回退到之前的时间戳+大小方案，保证逻辑不中断
            return file.lastModified() + "_" + file.length();
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (Exception ignored) {}
            }
        }
    }
}
