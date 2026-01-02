package top.yztz.msggo.exception;

public class DataLoadFailed extends Exception {
    public final String msg;

    public DataLoadFailed(String msg) {
        this.msg = msg;
    }
    public DataLoadFailed() {
        this.msg = "unknown error";
    }
}
