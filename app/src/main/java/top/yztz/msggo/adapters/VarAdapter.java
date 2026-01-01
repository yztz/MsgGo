package top.yztz.msggo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import top.yztz.msggo.data.DataLoader;
import top.yztz.msggo.R;


public class VarAdapter extends ListAdapter<String, VarAdapter.MyViewHolder> {
    private Context context;
//    IOnClickListener listener;

    public VarAdapter(Context context) {
        super(new DiffUtil.ItemCallback<String>() {
            @Override
            public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
//        this.listener = l;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_vars_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.mTvVar.setText(DataLoader.getTitles()[position]);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (listener != null) {
//                    listener.perform(position);
//                }
//
//            }
//        });
    }


    @Override
    public int getItemCount() {
        return DataLoader.getTitles().length;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvVar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvVar = itemView.findViewById(R.id.tv_var);
        }
    }

//    public interface IOnClickListener {
//        void perform(int position);
//    }
}
