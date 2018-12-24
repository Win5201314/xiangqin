package com.zsl.room.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.zsl.room.R;
import com.zsl.room.bean.HomeBean;

import java.util.List;

public class Hzadapter extends RecyclerView.Adapter<Hzadapter.ViewHolder> {

    private List<HomeBean> homeBeans;
    private Context context;

    public Hzadapter(List<HomeBean> homeBeans, Context context) {
        this.homeBeans = homeBeans;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bean_hz, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        HomeBean homeBean = homeBeans.get(i);

        viewHolder.show.setText(homeBean.toString());

        if(mOnItemClickListener != null){
            viewHolder.itemView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return homeBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView show;

        public ViewHolder(View itemView) {
            super(itemView);
            show = itemView.findViewById(R.id.show);
        }
    }

    OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener{
        void onClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
}
