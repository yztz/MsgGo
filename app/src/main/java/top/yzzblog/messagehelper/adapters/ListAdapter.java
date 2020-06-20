package top.yzzblog.messagehelper.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.HashMap;

import cn.refactor.library.SmoothCheckBox;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.data.DataModel;
import top.yzzblog.messagehelper.R;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListHolder> {
    private Context context;
    private DataModel dataModel;
    private SparseBooleanArray checkedMap;

    public ListAdapter(Context context) {
        this.context = context;
        checkedMap = new SparseBooleanArray();
        dataModel = DataLoader.getDataModel();

        for (int i = 0; i < getItemCount(); i++) {
            checkedMap.put(i, false);
        }
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListHolder(LayoutInflater.from(context).inflate(R.layout.layout_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ListHolder holder, final int position) {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
        holder.mRv.setLayoutManager(manager);
        HashMap<String, String> temp = dataModel.getMap(position);
        DataAdapter adapter = new DataAdapter(context, temp);
        holder.mRv.setAdapter(adapter);
        holder.mCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkedMap.put(position, isChecked);
            }
        });
        holder.mCb.setChecked(checkedMap.get(position));
    }


    public void setAllCheckBoxChosen(boolean flag) {
        for (int i = 0; i < checkedMap.size(); i++) {
            checkedMap.put(i, flag);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataModel.getSize();
    }

    class ListHolder extends RecyclerView.ViewHolder {
        private RecyclerView mRv;
        private CheckBox mCb;

        ListHolder(@NonNull View itemView) {
            super(itemView);

            mRv = itemView.findViewById(R.id.rv_single);
            mCb = itemView.findViewById(R.id.cb_check);
        }
    }

    public SparseBooleanArray getCheckedMap() {
        return checkedMap;
    }


}

class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataHolder> {
    private Context context;
    private HashMap<String, String> map;

    DataAdapter(Context context, HashMap<String, String> map) {
        this.context = context;
        this.map = map;
    }

    @NonNull
    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolder(LayoutInflater.from(context).inflate(R.layout.layout_data_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        String key = DataLoader.getTitles()[position];
        holder.mTvLabel.setText(key);
        holder.mTvData.setText(map.get(key));
    }

    @Override
    public int getItemCount() {
        return map.size();
    }

    class DataHolder extends RecyclerView.ViewHolder {
        private TextView mTvLabel, mTvData;

        DataHolder(@NonNull View itemView) {
            super(itemView);

            mTvLabel = itemView.findViewById(R.id.tv_label);
            mTvData = itemView.findViewById(R.id.tv_data);
        }
    }
}