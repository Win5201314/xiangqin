package com.socialassistant_youyuelive.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.UserID;

import java.util.ArrayList;
import java.util.List;

public class SuqareAnchorAdapter extends RecyclerView.Adapter<SuqareAnchorAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0; //说明带有头的
    private static final int TYPE_NORMAL = 1; //说明什么都没有的
    private static final int TYPE_FOOTER = 2; //说明带有底的
    //private Context context;
    private MyItemClickListener myItemClickListener;
    private List<UserID> mlist = new ArrayList<>();
    private DisplayImageOptions options = null;
    //头布局
    private View mHeaderView;
    //地布局
    private View mFooterView;
    //设置头布局的方法
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }
    //拿到头布局
    public View getHeaderView() {
        return mHeaderView;
    }
    //设置底布局的方法
    public void setFooterView(View footerView){
        mFooterView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }
    //拿到底布局
    public View getFooterView(){
        return mFooterView;
    }
    public boolean isHeader(int position) {
        return position == 0;
    }

    public boolean isFooter(int position) {
        return position == (mlist.size() + 1);
    }
    @Override
    public int getItemViewType(int position) {
//        if(mHeaderView == null) return TYPE_NORMAL;
//        if(position == 0) return TYPE_HEADER;
//        return TYPE_NORMAL;
        if (mHeaderView == null && mFooterView == null){
            return TYPE_NORMAL;
        }
        if (position == 0){
            //第一个item应该加载Header
            return TYPE_HEADER;
        }
        if (position == getItemCount()-1){
            //最后一个,应该加载Footer
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView Ivanchorimage;
        TextView Tvanchorname;
        TextView Tvanchorsign;
        TextView Tvanchorlikenum;
        ImageView Ivanchorlive;
        TextView Tvanchorlive;
        TextView Tvanchorlocation;
        TextView Tvanchorlocationnum;
        TextView TvanchorlocationKm;
        //每个Item的监听
        MyItemClickListener myItemClickListener;
        public ViewHolder(View view,MyItemClickListener myItemClickListener) {
            super(view);
            if(itemView == mHeaderView) return;
            if(itemView == mFooterView) return;
            Ivanchorimage = (ImageView) view.findViewById(R.id.suqare_anchor_image);
            Tvanchorname = (TextView) view.findViewById(R.id.suqare_anchor_name);
            Tvanchorsign = (TextView) view.findViewById(R.id.suqare_anchor_sign);
            Tvanchorlikenum = (TextView) view.findViewById(R.id.suqare_like_number);
            Ivanchorlive = (ImageView) view.findViewById(R.id.square_anchor_live_status_image);
            Tvanchorlive = (TextView) view.findViewById(R.id.square_anchor_live_status_text);
            Tvanchorlocation = (TextView) view.findViewById(R.id.suqare_location);
            Tvanchorlocationnum = (TextView) view.findViewById(R.id.suqare_anchor_location_distance);
            TvanchorlocationKm = (TextView) view.findViewById(R.id.suqare_anchor_location_km);
            this.myItemClickListener = myItemClickListener;
            view.setOnClickListener(this);
            //Animation animation = AnimationUtils.loadAnimation(DemoApplication.context, R.anim.en);
            //Ivanchorimage.startAnimation(animation);
        }

        @Override
        public void onClick(View view) {
            if(myItemClickListener != null){
                myItemClickListener.onItemClick(view,getPosition());
            }
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) return new ViewHolder(mHeaderView,myItemClickListener);
        if(mFooterView != null && viewType == TYPE_FOOTER) return new ViewHolder(mFooterView,myItemClickListener);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suqare_anchor_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view,myItemClickListener);
        return viewHolder;
    }
    public void addAll(List<UserID> list,boolean isClearDatasource/*,Context context*/){
        //this.context = context;
        if(isClearDatasource) mlist.clear();
        mlist.addAll(list);
        notifyDataSetChanged();
    }
    public void clear() {
        mlist.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER){
           return;
        } else if(getItemViewType(position) == TYPE_NORMAL){
            final int pos = getRealPosition(holder);
            UserID userID = mlist.get(pos);
            holder.Tvanchorname.setText(userID.getNickName());
            holder.Tvanchorsign.setText(userID.getSignature());
            holder.Tvanchorlikenum.setText(String.valueOf(userID.getFriendsAccount()));
            if(!TextUtils.isEmpty(userID.getProvince())){
                if (userID.getProvince().contains("其他")) {
                    holder.Tvanchorlocation.setText(userID.getCity());
                } else {
                    holder.Tvanchorlocation.setText(userID.getCity());
                }
            }else {
                holder.Tvanchorlocation.setText("在火星..");
            }
            //（0视频中 1可视频 2免打扰 3离线）
            if (userID.getChat_status() == 0) {
                holder.Ivanchorlive.setImageResource(R.mipmap.in_the_video);
                holder.Tvanchorlive.setText("视频中");
            } else if (userID.getChat_status() == 1) {
                holder.Ivanchorlive.setImageResource(R.mipmap.in_the_online);
                holder.Tvanchorlive.setText("在线中");
            } else if (userID.getChat_status() == 2) {
                holder.Ivanchorlive.setImageResource(R.mipmap.no_disturbing);
                holder.Tvanchorlive.setText("免打扰");
            } else if (userID.getChat_status() == 3){
                holder.Ivanchorlive.setImageResource(R.mipmap.off_line);
                holder.Tvanchorlive.setText("离线");
            }
            //跟主播的距离显示
            if(userID.getDistance() != 0f){
                holder.Tvanchorlocationnum.setText(String.valueOf(userID.getDistance()));
                holder.Tvanchorlocationnum.setVisibility(View.VISIBLE);
                holder.TvanchorlocationKm.setVisibility(View.VISIBLE);
            }else {
                holder.Tvanchorlocationnum.setVisibility(View.GONE);
                holder.TvanchorlocationKm.setVisibility(View.GONE);
            }
            //显示图片的配置
            if (options == null) {
                options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.unloading_bg)
                /*.showImageOnFail(R.drawable.ic_error)*/
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .build();
            }
            ImageLoader.getInstance().displayImage(userID.getFace_url(),holder.Ivanchorimage, options);
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.sq_en);
//            holder.Ivanchorimage.startAnimation(animation);
        }else {
            return;
        }
    }
    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }


    @Override
    public int getItemCount() {
        //正常
        // return mlist.size();
        //当只加一个头布局时
        //return mHeaderView == null ? mlist.size() : mlist.size() + 1;
        if(mHeaderView == null && mFooterView == null){
            return mlist.size();
        }else if(mHeaderView == null && mFooterView != null){
            return mlist.size() + 1;
        }else if (mHeaderView != null && mFooterView == null){
            return mlist.size() + 1;
        }else {
            return mlist.size() + 2;
        }
    }
    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }
    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

}
