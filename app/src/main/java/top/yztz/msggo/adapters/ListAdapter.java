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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.R;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListHolder> {
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListHolder(LayoutInflater.from(context).inflate(R.layout.layout_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ListHolder holder, final int position) {
        // Disable nested scrolling to let parent handle horizontal scroll
        holder.mRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
        
        HashMap<String, String> temp = DataModel.getRow(holder.getBindingAdapterPosition());
        DataAdapter adapter = new DataAdapter(context, temp);
        holder.mRv.setAdapter(adapter);

        // Make the inner RecyclerView pass touch events to the itemView
        holder.mRv.setOnTouchListener((v, event) -> holder.itemView.onTouchEvent(event));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.getAbsoluteAdapterPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return DataModel.loaded() ? DataModel.getRowCount() : 0;
    }

    static class ListHolder extends RecyclerView.ViewHolder {
        private RecyclerView mRv;

        ListHolder(@NonNull View itemView) {
            super(itemView);
            mRv = itemView.findViewById(R.id.rv_single);
        }
    }
}

class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataHolder> {
    private Context context;
    private HashMap<String, String> map;
    private String[] titles;

    DataAdapter(Context context, HashMap<String, String> map) {
        this.context = context;
        this.map = map;
        this.titles = DataModel.getTitles();
    }

    @NonNull
    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolder(LayoutInflater.from(context).inflate(R.layout.layout_data_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        String key = titles[position];
        String value = map.get(key);
        holder.mTvData.setText(value != null ? value : "");
    }

    @Override
    public int getItemCount() {
        return titles == null ? 0 : titles.length;
    }

    static class DataHolder extends RecyclerView.ViewHolder {
        private TextView mTvData;

        DataHolder(@NonNull View itemView) {
            super(itemView);
            mTvData = itemView.findViewById(R.id.tv_data);
        }
    }
}