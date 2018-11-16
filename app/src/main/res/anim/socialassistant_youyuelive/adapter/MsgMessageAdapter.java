package com.socialassistant_youyuelive.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.Message;
import com.socialassistant_youyuelive.commomentity.Orders;
import com.socialassistant_youyuelive.commomentity.UserID;

import java.util.ArrayList;
import java.util.List;

public class MsgMessageAdapter extends BaseAdapter {
    private Context context;
    private List<Orders> msgList = new ArrayList<>();
    private DisplayImageOptions options = null;
    public MsgMessageAdapter(Context context){
        this.context = context;
    }
    @Override
    public int getCount() {
        return msgList.size();
    }

    @Override
    public Orders getItem(int position) {
        return msgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = minflater.inflate(R.layout.msg_list_item,null);
            holder.TVtext = (TextView) convertView.findViewById(R.id.msg_anchorname_text);
            holder.TVsign = (TextView) convertView.findViewById(R.id.msg_anchorsign_text);
            holder.IVavatar = (ImageView) convertView.findViewById(R.id.msg_anchor_image);
            holder.TVtime = (TextView) convertView.findViewById(R.id.msg_chat_time);

            holder.TVunread = (TextView) convertView.findViewById(R.id.msg_unread_msg);
            holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.msg_unread_layout);
            holder.IvLive = (ImageView) convertView.findViewById(R.id.msg_live_status);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        Orders orders = msgList.get(position);
        holder.TVtext.setText(orders.getNickName());
        //判断是否是主播消息------用处,显示最新消息
        if(orders.getMessage() == null){
            holder.TVsign.setText(orders.getUsertext());
        }else {
            holder.TVsign.setText(orders.getMessage());
        }
        holder.TVtime.setText(orders.getTime());
        if(orders.getUnread() != 0){
            holder.linearLayout.setVisibility(View.VISIBLE);
            holder.TVunread.setText(orders.getUnread() + "");
        }else {
            holder.linearLayout.setVisibility(View.INVISIBLE);
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
        ImageLoader.getInstance().displayImage(orders.getFaceUrl(),holder.IVavatar);
        return convertView;
    }
    static class ViewHolder {
        public TextView TVtext,TVsign,TVtime,TVunread;
        public ImageView IVavatar,IvLive;
        public LinearLayout linearLayout;
    }
    public void addAll(List<Orders> mlist,boolean isClearDatasource){
        if(isClearDatasource){
            msgList.clear();
        }
        msgList.addAll(mlist);
        notifyDataSetChanged();
    }
}
