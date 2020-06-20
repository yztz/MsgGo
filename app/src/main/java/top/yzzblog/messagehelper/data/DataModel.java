package top.yzzblog.messagehelper.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DataModel implements Serializable {

    private ArrayList<HashMap<String, String >> data;

    public DataModel(ArrayList<HashMap<String, String >> data) {
        this.data = data;
    }

    public int getSize() {
        return data.size();
    }

    public HashMap<String, String> getMap(int index) {
        return data.get(index);
    }
}
