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
import com.zsl.activity.SubmitActivity;
import com.zsl.bean.UserImage;
import com.zsl.xiangqin.R;

import java.util.List;

public class UserImageAdapter extends RecyclerView.Adapter<UserImageAdapter.ViewHolder> {

    private List<UserImage> userImages;
    private Context context;

    public UserImageAdapter(List<UserImage> userImages, Context context) {
        this.userImages = userImages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bean_image, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        UserImage userImage = userImages.get(i);
        //下载图片
        Picasso.with(context).load(userImage.getImageUrl()).into(viewHolder.pic);
        //viewHolder.pic.setImageResource(R.mipmap.sc2);
        viewHolder.jx.setText(String.valueOf("家乡:" + userImage.getJx()));
        viewHolder.xjd.setText(String.valueOf("现居地:" + userImage.getXjd()));
        viewHolder.birthday.setText(String.valueOf("出生年月:" + userImage.getBirthday()));

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
        return userImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView pic;
        AppCompatTextView jx;
        AppCompatTextView xjd;
        AppCompatTextView birthday;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            jx = itemView.findViewById(R.id.jx);
            xjd = itemView.findViewById(R.id.xjd);
            birthday = itemView.findViewById(R.id.birthday);
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
