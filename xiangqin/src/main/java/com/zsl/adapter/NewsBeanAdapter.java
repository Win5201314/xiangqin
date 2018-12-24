package com.zsl.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.bean.NewsBean;
import com.zsl.xiangqin.R;

import java.util.List;

public class NewsBeanAdapter extends RecyclerView.Adapter<NewsBeanAdapter.ViewHolder> {

    private List<NewsBean> newsBeans;
    private Context context;

    public NewsBeanAdapter(List<NewsBean> newsBeans, Context context) {
        this.newsBeans = newsBeans;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bean_news, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        NewsBean newsBean = newsBeans.get(i);
        viewHolder.data.setText(String.valueOf(newsBean.getData()));

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
        return newsBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView data;

        public ViewHolder(View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.data);
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
