package top.yzzblog.messagehelper.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.util.TextParser;


public class MessageService extends IntentService {

    public MessageService() {
        super("MessageService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 以多线程的方式处理intent
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        ArrayList<Integer> itemIndices = null;
        try {
            itemIndices = (ArrayList<Integer>) intent.getSerializableExtra("itemIndices");

        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= itemIndices.size(); i++) {
            try {
                String rawContent = DataLoader.getContent();
                HashMap<String, String> tmp = DataLoader.getDataModel().getMap(itemIndices.get(i - 1));

                String content = TextParser.parse(rawContent, tmp);
                String phoneNumber = tmp.get(DataLoader.getNumberColumn());
                SMSSender.sendMessage(getApplicationContext(), content, phoneNumber, i);

//                //test
//                Intent it = new Intent(TEST);
//                it.putExtra("code", i);
//                it.putExtra("content", content);
//                it.putExtra("number", phoneNumber);
//                sendBroadcast(it);

                Thread.sleep(500);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
}
