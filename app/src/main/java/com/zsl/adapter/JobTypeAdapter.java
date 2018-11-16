package com.zsl.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.bean.JobBean;
import com.zsl.bean.JobTypeBean;
import com.zsl.itgod.R;
import com.zsl.itgod.ShowActivity;

import java.util.List;


public class JobTypeAdapter extends RecyclerView.Adapter<JobTypeAdapter.ViewHolder> {

    private List<JobTypeBean> jobTypeBeans;
    private Context context;

    public JobTypeAdapter(List<JobTypeBean> jobTypeBeans, Context context) {
        this.jobTypeBeans = jobTypeBeans;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.jobbean, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                JobTypeBean jobTypeBean = jobTypeBeans.get(position);
                Intent intent = new Intent(context, ShowActivity.class);
                intent.putExtra("type", jobTypeBean.getType());
                context.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        JobTypeBean jobTypeBean = jobTypeBeans.get(i);
        switch (jobTypeBean.getType()) {
            case 0: {
                viewHolder.pic.setBackgroundResource(R.mipmap.android);
                break;
            }
            case 1: {
                viewHolder.pic.setBackgroundResource(R.mipmap.ios);
                break;
            }
            case 2: {
                viewHolder.pic.setBackgroundResource(R.mipmap.java);
                break;
            }
            case 3: {
                viewHolder.pic.setBackgroundResource(R.mipmap.web);
                break;
            }
            case 4: {
                viewHolder.pic.setBackgroundResource(R.mipmap.ui);
                break;
            }
            case 5: {
                viewHolder.pic.setBackgroundResource(R.mipmap.cpp);
                break;
            }
            case 6: {
                viewHolder.pic.setBackgroundResource(R.mipmap.cl);
                break;
            }
            case 7: {
                viewHolder.pic.setBackgroundResource(R.mipmap.nx);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return jobTypeBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView pic;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
        }
    }

}

