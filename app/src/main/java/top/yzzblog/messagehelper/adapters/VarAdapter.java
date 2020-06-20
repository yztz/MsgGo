package top.yzzblog.messagehelper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;


public class VarAdapter extends RecyclerView.Adapter<VarAdapter.MyViewHolder> {
    private Context context;
    IOnClickListener listener;

    public VarAdapter(Context context, IOnClickListener l) {
        this.context = context;
        this.listener = l;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_vars_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.mTvVar.setText(DataLoader.getTitles()[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.perform(position);
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return DataLoader.getTitles().length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvVar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvVar = itemView.findViewById(R.id.tv_var);
        }
    }

    public interface IOnClickListener {
        void perform(int position);
    }
}
