package com.zsl.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.bean.Detail;
import com.zsl.xiangqin.R;

import java.util.List;

public class LikeMeAdapter extends RecyclerView.Adapter<LikeMeAdapter.ViewHolder> {

    private List<Detail> details;
    private Context context;

    public LikeMeAdapter(List<Detail> details, Context context) {
        this.details = details;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bean_likeme, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Detail detail = details.get(i);
        //下载图片
        viewHolder.pic.setImageResource(R.mipmap.sc2);
        StringBuffer sb = new StringBuffer();
        sb.append("名字：" + detail.getName() + "\n")
                .append("手机号：" + detail.getPhone() + "\n")
                .append("出生年月：" + detail.getBirthday() + "\n")
                .append("家乡：" + detail.getPath() + "\n")
                .append("现居地：" + detail.getResidence() + "\n")
                .append("学历：" + detail.getEducation() + "\n")
                .append("车房：" + detail.getCar() + "\n")
                .append("职业工作：" + detail.getOccupation() + "\n")
                .append("月平均薪水：" + detail.getSalary() + "\n");
        viewHolder.message.setText(sb.toString());

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

        AppCompatImageView pic;
        AppCompatTextView message;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            message = itemView.findViewById(R.id.message);
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
