package com.socialassistant_youyuelive.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.AboutAnchordActivity;
import com.socialassistant_youyuelive.commomentity.CircleImageView;
import com.socialassistant_youyuelive.commomentity.Orders;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天内容适配器
 */

public class Msginfoadapter<T extends Orders> extends BaseAdapter{
    //上下文
    private Context context;
    //数据源
    private List<T> datas = new ArrayList<>();
    //使用viewHolder来优化listview
    private ViewHolder viewHolder;
    private DisplayImageOptions options = null;

    public Msginfoadapter(Context context,List<T> list) {
        this.context = context;
        this.datas = list;
    }
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public T getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.msg_info_item,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
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
        final Orders orders = getItem(position);
        if(orders.getMessage() != null){
            viewHolder.anchorbusylayout.setVisibility(View.GONE);
            viewHolder.tvAnchorMsg.setText(orders.getMessage());
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
            viewHolder.tvAnchorMsg.startAnimation(animation);
            ImageLoader.getInstance().displayImage(orders.getFaceUrl(),viewHolder.ivAnchorImage,options);
            viewHolder.anchorLayout.setVisibility(View.VISIBLE);
            viewHolder.userLayout.setVisibility(View.GONE);
        }
        if(orders.getUsertext() != null){
            viewHolder.tvUserMsg.setText(orders.getUsertext());
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
            if(orders.getUsertext().equals("主播如果未回复,请于他(她)进行视频通话吧!")){
                viewHolder.userLayout.setVisibility(View.GONE);
                viewHolder.anchorLayout.setVisibility(View.GONE);
                viewHolder.anchorbusylayout.setVisibility(View.VISIBLE);
                //viewHolder.tvUserMsg.setGravity(View.TEXT_ALIGNMENT_CENTER);
            }else {
                viewHolder.tvUserMsg.startAnimation(animation);
                viewHolder.anchorbusylayout.setVisibility(View.GONE);
                if(TextUtils.isEmpty(orders.getUserfaceUrl())){
                    viewHolder.ivUserImage.setImageResource(R.mipmap.unloading_bg);
                }else {
                    ImageLoader.getInstance().displayImage(orders.getUserfaceUrl(),viewHolder.ivUserImage,options);
                }
                viewHolder.anchorLayout.setVisibility(View.GONE);
                viewHolder.userLayout.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.ivAnchorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.item_chat_anchor_image){
                    if(orders.getAmchorId() == null || orders.getAmchorId().equals("")){
                        return;
                    }
                    HttpUtil.getAnchorData(new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            if(s != null && !s.equals("")){
                                JSONObject jsonobject = JSON.parseObject(s);
                                if(jsonobject == null) return;
                                String state = jsonobject.getString("state");
                                if(state != null && state.equals("ok")){
                                    loadUserId(jsonobject,state);
                                }
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                        }
                    }, orders.getAmchorId());
                }
            }
        });
        return convertView;
    }
    public void addAll(List<T> list,boolean isClearDatasource){
        if(isClearDatasource) datas.clear();
        datas.addAll(list);
        notifyDataSetChanged();
    }
    static class ViewHolder{
        View rootView;
        CircleImageView ivAnchorImage,ivUserImage;
        TextView tvAnchorMsg,tvUserMsg;
        LinearLayout anchorLayout,userLayout,anchorbusylayout;
        ViewHolder(View rootView){
            this.rootView = rootView;
            this.ivAnchorImage = (CircleImageView) rootView.findViewById(R.id.item_chat_anchor_image);
            this.ivUserImage = (CircleImageView) rootView.findViewById(R.id.item_chat_user_image);
            this.tvAnchorMsg = (TextView) rootView.findViewById(R.id.item_chat_anchor_msg);
            this.tvUserMsg = (TextView) rootView.findViewById(R.id.item_chat_user_msg);
            this.anchorLayout = (LinearLayout) rootView.findViewById(R.id.item_chat_anchor_layout);
            this.userLayout = (LinearLayout) rootView.findViewById(R.id.item_chat_user_layout);
            this.anchorbusylayout = (LinearLayout) rootView.findViewById(R.id.anchor_busy_layout);
        }
    }
    private void loadUserId(JSONObject jsonobject, String state) {
        String  userjson = jsonobject.getString("anchorinfo");
        JSONArray jsonarray = JSON.parseArray(userjson);
        for (int i = 0;i < jsonarray.size();i++){
            JSONObject jo = JSON.parseObject(jsonarray.get(i).toString());
            UserID userID = new UserID();
            //获取5个头像,已逗号分隔
            userID.setAlbum(jo.getString("album"));
            //主播头像
            userID.setFace_url(jo.getString("headImgUrl"));
            //主播昵称
            userID.setNickName(jo.getString("nickName"));
            //主播签名
            userID.setSignature(jo.getString("signature"));
            //主播点赞数量
            userID.setFriendsAccount(jo.getIntValue("friendsAccount"));
            //主播城市
            userID.setCity(jo.getString("city"));
            //主播省份
            userID.setProvince(jo.getString("province"));
            //主播ID
            userID.setAnchorId(jo.getIntValue("anchorId"));
            //主播手机号
            userID.setMobile(jo.getString("mobile"));
            //主播tokenID
            userID.setTokenId(jo.getString("tokenId"));
            //主播的三个标签,已逗号形式分开
            userID.setLabels(jo.getString("labels"));
            //获取语音URL
            userID.setVoiceUrl(jo.getString("voiceUrl"));
            //获取视频URL
            userID.setHeadVedio(jo.getString("headVedio"));
            if(jo.getBooleanValue("sex")){
                userID.setMan(true);
            }else {
                userID.setMan(false);
            }
            //判断是否正在直播
            if (jo.getIntValue("chat_status") == 0) {
                userID.setLive(true);
            }
            context.startActivity(new Intent(context,AboutAnchordActivity.class).putExtra("user_info",userID));
        }
    }
}
