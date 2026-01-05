package top.yztz.msggo.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import top.yztz.msggo.R;
import top.yztz.msggo.data.DataModel;

public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.CheckboxHolder> {
    private Context context;
    private SparseBooleanArray checkedMap;
    // Callback to notify activity about selection changes if needed
    private OnSelectionChangedListener selectionListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int position, boolean isChecked);
    }

    public CheckboxAdapter(Context context) {
        this.context = context;
        checkedMap = new SparseBooleanArray();

        if (DataModel.loaded()) {
            for (int i = 0; i < getItemCount(); i++) {
                checkedMap.put(i, false);
            }
        }
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public CheckboxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CheckboxHolder(LayoutInflater.from(context).inflate(R.layout.layout_checkbox_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CheckboxHolder holder, int position) {
        holder.mCb.setOnCheckedChangeListener(null);
        holder.mCb.setChecked(checkedMap.get(position));
        holder.mCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedMap.put(holder.getAbsoluteAdapterPosition(), isChecked);
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(holder.getAbsoluteAdapterPosition(), isChecked);
            }
        });

        // Allow clicking the item view to toggle checkbox
        holder.itemView.setOnClickListener(v -> holder.mCb.toggle());
    }

    @Override
    public int getItemCount() {
        return DataModel.loaded() ? DataModel.getRowCount() : 0;
    }

    public void setAllCheckBoxChosen(boolean flag) {
        for (int i = 0; i < checkedMap.size(); i++) {
            checkedMap.put(i, flag);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getCheckedMap() {
        return checkedMap;
    }

    static class CheckboxHolder extends RecyclerView.ViewHolder {
        CheckBox mCb;

        CheckboxHolder(@NonNull View itemView) {
            super(itemView);
            mCb = itemView.findViewById(R.id.cb_check);
        }
    }
}
