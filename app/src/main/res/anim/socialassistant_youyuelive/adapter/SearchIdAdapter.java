package com.socialassistant_youyuelive.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.CircleImageView;
import com.socialassistant_youyuelive.commomentity.UserID;

import java.util.List;

public class SearchIdAdapter extends BaseAdapter {

    private Context context;
    private List<UserID> ids;
    //显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder()
                /*.showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error)*/
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    public SearchIdAdapter(Context context, List<UserID> ids) {
        this.context = context;
        this.ids = ids;
    }
    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public UserID getItem(int position) {
        return ids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(this.context).inflate(R.layout.search_id_item, null);
            holder.anchorImage = (ImageView) view.findViewById(R.id.follow_anchor_image);
            holder.anchorName = (TextView) view.findViewById(R.id.follow_anchor_name_text);
            holder.anchorAge = (TextView) view.findViewById(R.id.follow_anchor_age);
            holder.anchorSign = (TextView) view.findViewById(R.id.follow_anchor_sign_text);
            holder.anchorisLive = (ImageView) view.findViewById(R.id.follow_anchor_islive);
            holder.onLivelayout = (RelativeLayout) view.findViewById(R.id.follow_live_btn_on_layout);
            holder.offLivelayout = (RelativeLayout) view.findViewById(R.id.follow_live_btn_off_layout);
            holder.anchorSamllImage1 = (ImageView) view.findViewById(R.id.follow_anchor_image_1);
            holder.anchorSamllImage2 = (ImageView) view.findViewById(R.id.follow_anchor_image_2);
            holder.anchorSamllImage3 = (ImageView) view.findViewById(R.id.follow_anchor_image_3);
            holder.anchorSamllImage4 = (ImageView) view.findViewById(R.id.follow_anchor_image_4);
            holder.anchorSamllImage5 = (ImageView) view.findViewById(R.id.follow_anchor_image_5);
            holder.anchorLabel1 = (TextView) view.findViewById(R.id.follow_anchor_label_1);
            holder.anchorLabel2 = (TextView) view.findViewById(R.id.follow_anchor_label_2);
            holder.anchorLabel3 = (TextView) view.findViewById(R.id.follow_anchor_label_3);
            holder.labelLayout1 = (RelativeLayout) view.findViewById(R.id.follow_anchor_big_label_1);
            holder.labelLayout2 = (RelativeLayout) view.findViewById(R.id.follow_anchor_big_label_2);
            holder.labelLayout3 = (RelativeLayout) view.findViewById(R.id.follow_anchor_big_label_3);
            holder.labelLayout1.setVisibility(View.INVISIBLE);
            holder.labelLayout2.setVisibility(View.INVISIBLE);
            holder.labelLayout3.setVisibility(View.INVISIBLE);
            holder.anchorLocation = (TextView) view.findViewById(R.id.follow_anchor_location);
            holder.sex_type = (ImageView) view.findViewById(R.id.sex_type);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        UserID id = ids.get(position);
        ImageLoader.getInstance().displayImage(id.getFace_url(), holder.anchorImage, options);
        String a = id.getAlbum();
        if (a != null && !a.equals("")) {
            String[] albums = a.split(",");
            if (albums != null && albums.length > 0) {
                for (int i = 0; i < albums.length; i++) {
                    String url_img = albums[i];
                    if (url_img != null && !url_img.equals("")) {
                        switch (i) {
                            case 0:
                                ImageLoader.getInstance().displayImage(url_img, holder.anchorSamllImage1, options);
                                break;
                            case 1:
                                ImageLoader.getInstance().displayImage(url_img, holder.anchorSamllImage2, options);
                                break;
                            case 2:
                                ImageLoader.getInstance().displayImage(url_img, holder.anchorSamllImage3, options);
                                break;
                            case 3:
                                ImageLoader.getInstance().displayImage(url_img, holder.anchorSamllImage4, options);
                                break;
                            case 4:
                                ImageLoader.getInstance().displayImage(url_img, holder.anchorSamllImage5, options);
                                break;
                        }
                    }
                }
            }
        }
        if (id.isLive()) {
            holder.anchorisLive.setVisibility(View.VISIBLE);
            holder.onLivelayout.setVisibility(View.VISIBLE);
            holder.offLivelayout.setVisibility(View.GONE);
        } else {
            holder.anchorisLive.setVisibility(View.INVISIBLE);
            holder.onLivelayout.setVisibility(View.GONE);
            holder.offLivelayout.setVisibility(View.VISIBLE);
        }
        if (id.isMan()) {//男
            holder.sex_type.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.man_w));
        } else {//女
            holder.sex_type.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.anchor_sex_man));
        }
        String d = "";
        d = id.getNickName();
        d = (d != null && !d.equals("")) ? d : "小空";
        holder.anchorName.setText(d);
        holder.anchorAge.setText(String.valueOf(id.getYears()));
        d = id.getSignature();
        d = (d != null && !d.equals("")) ? d : "I have a dream!";
        holder.anchorSign.setText(d);
        d = id.getProvince();
        d = (d != null && !d.equals("")) ? d : "广东";
        String s = id.getCity();
        s = (s != null && !s.equals("")) ? s : "深圳";
        d = d +  "," + s;
        holder.anchorLocation.setText(d);
        d = id.getLabels();
        if (d != null && !d.equals("")) {
            String[] labels = d.split(",");
            if (labels != null && labels.length > 0) {
                for (int i = 0; i < labels.length; i++) {
                    d = labels[i];
                    if (d != null && !d.equals("")) {
                        switch (i) {
                            case 0:
                                holder.labelLayout1.setVisibility(View.VISIBLE);
                                holder.anchorLabel1.setText(d);
                                break;
                            case 1:
                                holder.labelLayout2.setVisibility(View.VISIBLE);
                                holder.anchorLabel2.setText(d);
                                break;
                            case 2:
                                holder.labelLayout3.setVisibility(View.VISIBLE);
                                holder.anchorLabel3.setText(d);
                                break;
                        }
                    }
                }
            }
        }
        return view;
    }

    private static class ViewHolder {
        //主播头像
        ImageView anchorImage;
        //主播名字
        TextView anchorName;
        //主播年龄
        TextView anchorAge;
        //主播性别
        ImageView sex_type;
        //主播签名
        TextView anchorSign;
        //主播是否正在直播
        ImageView anchorisLive;
        RelativeLayout onLivelayout;
        RelativeLayout offLivelayout;
        //主播小图片
        ImageView anchorSamllImage1;
        ImageView anchorSamllImage2;
        ImageView anchorSamllImage3;
        ImageView anchorSamllImage4;
        ImageView anchorSamllImage5;
        //主播标签
        TextView anchorLabel1;
        TextView anchorLabel2;
        TextView anchorLabel3;
        //主播标签大布局
        RelativeLayout labelLayout1;
        RelativeLayout labelLayout2;
        RelativeLayout labelLayout3;
        //主播定位
        TextView anchorLocation;
    }

}
