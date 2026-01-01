package top.yztz.msggo.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SendingMonitor {
    private static final SendingMonitor instance = new SendingMonitor();
    
    public static SendingMonitor getInstance() {
        return instance;
    }

    private final MutableLiveData<SendingState> sendingState = new MutableLiveData<>();
    private final MutableLiveData<String> logs = new MutableLiveData<>();
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(); // Current index
    private final MutableLiveData<Integer> total = new MutableLiveData<>();

    private SendingMonitor() {
        reset();
    }

    public void reset() {
        sendingState.postValue(SendingState.IDLE);
        logs.postValue("");
        progress.postValue(0);
        total.postValue(0);
    }

    public void setTotal(int totalCount) {
        total.postValue(totalCount);
    }

    public void updateProgress(int current) {
        progress.postValue(current);
    }

    public void appendLog(String log) {
        String current = logs.getValue();
        if (current == null) current = "";
        logs.postValue(current + log + "\n");
    }

    public void setStatus(SendingState state) {
        sendingState.postValue(state);
    }

    public LiveData<SendingState> getState() {
        return sendingState;
    }

    public LiveData<String> getLogs() {
        return logs;
    }
    
    public LiveData<Integer> getProgress() {
        return progress;
    }
    
    public LiveData<Integer> getTotal() {
        return total;
    }

    public enum SendingState {
        IDLE, SENDING, PAUSED, COMPLETED, CANCELLED, ERROR
    }
}
