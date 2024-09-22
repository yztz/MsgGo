package top.yzzblog.messagehelper.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import top.yzzblog.messagehelper.data.Message;
import top.yzzblog.messagehelper.util.FileUtil;


public class MessageWorker extends Worker {
    private static final String TAG = "SMSWorker";

    public MessageWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 获取传递的参数
        int delay = getInputData().getInt("delay", 5000);
        int subId = getInputData().getInt("subId", SMSSender.getDefaultSubID());
        String serPath = getInputData().getString("message_file");
        if (serPath == null) {
            Log.d(TAG, "doWork: serPath is null");
            return Result.failure();
        }

        Message[] messages = FileUtil.readMessageArrayFromFile(getApplicationContext(), serPath);
        if (messages == null) {
            Log.d(TAG, "doWork: messages is null");
            return Result.failure();
        }

        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            try {

                Log.d(TAG, String.format("ready to send message-%d to %s, message content: %s ", i, message.getPhone(), message.getContent()));
                SMSSender.sendMessage(getApplicationContext(), message.getContent(), message.getPhone(), subId,i + 1);

                Thread.sleep(delay);  // 模拟发送短信的延迟

            } catch (InterruptedException e) {
                e.printStackTrace();
                return Result.failure();  // 任务失败，重试
            }
        }

        // delete file after done
        boolean isDeleted = getApplicationContext().deleteFile(serPath);
        if (isDeleted) {
            Log.d(TAG, "File " + serPath + " has been deleted.");
        } else {
            Log.d(TAG, "Failed to delete file: " + serPath);
        }

        return Result.success();  // 任务成功
    }
}
