package com.zsl.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.bean.JobBean;
import com.zsl.itgod.R;

import java.util.List;


public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ViewHolder> {

    private List<JobBean> jobBeans;
    private Context context;

    public JobAdapter(List<JobBean> jobBeans, Context context) {
        this.jobBeans = jobBeans;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.jsbean, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        JobBean jobBean = jobBeans.get(i);
        viewHolder.name.setText("公司名字:" + jobBean.getCompanyName());
        viewHolder.salary.setText("薪资范围:" + jobBean.getSalary());
        viewHolder.address.setText("办公地址:" + jobBean.getAddress());
        viewHolder.outsource.setText(jobBean.isOutsource() ? "外包性质" : "非外包性质");

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
        return jobBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView name;
        AppCompatTextView salary;
        AppCompatTextView address;
        AppCompatTextView outsource;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.companyName);
            salary = itemView.findViewById(R.id.salary);
            address = itemView.findViewById(R.id.address);
            outsource = itemView.findViewById(R.id.outsource);
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
