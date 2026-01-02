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
    private final MutableLiveData<Integer> total = new MutableLiveData<>();
    
    // Sent progress: how many have been queued for sending
    private final MutableLiveData<Integer> sentProgress = new MutableLiveData<>();
    // Confirmed progress: how many callbacks received (success or fail)
    private final MutableLiveData<Integer> confirmedProgress = new MutableLiveData<>();
    // Success count
    private final MutableLiveData<Integer> successCount = new MutableLiveData<>();

    private SendingMonitor() {
        reset();
    }

    public void reset() {
        sendingState.postValue(SendingState.IDLE);
        logs.postValue("");
        total.postValue(0);
        sentProgress.postValue(0);
        confirmedProgress.postValue(0);
        successCount.postValue(0);
    }

    public void setTotal(int totalCount) {
        total.postValue(totalCount);
    }

    public void updateSentProgress(int current) {
        sentProgress.postValue(current);
    }

    public void incrementConfirmed(boolean success) {
        Integer currentConfirmed = confirmedProgress.getValue();
        if (currentConfirmed == null) currentConfirmed = 0;
        confirmedProgress.postValue(currentConfirmed + 1);
        
        if (success) {
            Integer currentSuccess = successCount.getValue();
            if (currentSuccess == null) currentSuccess = 0;
            successCount.postValue(currentSuccess + 1);
        }
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

    public LiveData<Integer> getSentProgress() {
        return sentProgress;
    }
    
    public LiveData<Integer> getConfirmedProgress() {
        return confirmedProgress;
    }
    
    public LiveData<Integer> getSuccessCount() {
        return successCount;
    }

    public LiveData<Integer> getTotal() {
        return total;
    }

    public enum SendingState {
        IDLE, SENDING, PAUSED, COMPLETED, CANCELLED, ERROR
    }
}
