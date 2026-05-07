/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.CheckboxHolder> {
    private final Context context;
    private final SparseBooleanArray checkedMap;
    private int itemCount;
    private OnSelectionChangedListener selectionListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int position, boolean isChecked);
    }

    public CheckboxAdapter(Context context, int itemCount) {
        this.context = context;
        this.itemCount = itemCount;
        checkedMap = new SparseBooleanArray();
        for (int i = 0; i < itemCount; i++) {
            checkedMap.put(i, false);
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
        holder.itemView.setOnClickListener(v -> holder.mCb.toggle());
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public void setAllCheckBoxChosen(boolean flag) {
        for (int i = 0; i < itemCount; i++) {
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
