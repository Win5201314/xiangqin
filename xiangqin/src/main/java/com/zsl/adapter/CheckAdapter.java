package com.zsl.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.zsl.bean.BeforeDetail;
import com.zsl.xiangqin.R;

import java.util.List;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {

    private List<BeforeDetail> details;
    private Context context;

    public CheckAdapter(List<BeforeDetail> details, Context context) {
        this.details = details;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bean_check, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        BeforeDetail detail = details.get(i);
        //下载图片
        Picasso.with(context).load(detail.getImageUrl().split(";")[0]).into(viewHolder.face);
        StringBuffer sb = new StringBuffer();
        sb.append("名字: "+ detail.getName() + "\n")
                .append("性别: " + detail.getSex() + "\n")
                .append("出生年月: " + detail.getBirthday() + "\n")
                .append("家乡: " + detail.getPath() + "\n")
                .append("现居地: " + detail.getResidence() + "\n")
                .append("学历: " + detail.getEducation() + "\n")
                .append("职业工作: " + detail.getOccupation() + "\n")
                .append("平均月薪: " + detail.getSalary() + "\n")
                .append("车房情况: " + detail.getCar() + "\n");
        String s = sb.toString();
        viewHolder.about.setText(s);

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
        return details.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView face;
        AppCompatTextView about;

        public ViewHolder(View itemView) {
            super(itemView);
            face = itemView.findViewById(R.id.face);
            about = itemView.findViewById(R.id.about);
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
