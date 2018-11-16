package com.socialassistant_youyuelive.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.socialassistant_youyuelive.AV.AVchat;
import com.socialassistant_youyuelive.AV.DemoCache;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActionSheetDialog;
import com.socialassistant_youyuelive.commomentity.CircleImageView;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.RoundImageView;
import com.socialassistant_youyuelive.commomentity.RoundProgressBar;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.fragments.ListFragmentMe;
import com.socialassistant_youyuelive.util.HttpDownloader;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.OkHttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;
import com.socialassistant_youyuelive.wxapi.WXEntryActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AnchordetailsActivity extends LeftSlipActivity implements View.OnClickListener {

    private static final int VOICE = 3;
    public static final int SUCCESS = 4;
    private Context context;

    public Handler hander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VOICE:{
                    if (isMe) {
                        call_btn.setVisibility(View.INVISIBLE);
                        live_btn.setVisibility(View.INVISIBLE);
                    } else {
                        if (isShake) {
                            isShake = false;
                            call_btn.startAnimation(shake);
                        } else {
                            isShake = true;
                            live_btn.startAnimation(shake);
                        }
                    }
                    File file = new File(BecomeAnchorActivity.getSDPath() + ConstString.PATH_AUDIO + File.separator + anchorId + ".mp3");
                    if (file == null || !file.exists()) return;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(fis.getFD());
                        mediaPlayer.prepare();
                        //mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (timer != null) timer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (timer != null) timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AVchat.isForceLoginAvchatActivity()){
            // 弹出强制退出对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("安全提示")
                    .setMessage(R.string.avchat_login_other_place)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // 退出当前账号
                            ListFragmentMe.logout(AnchordetailsActivity.this, true);
                        }
                    });
            android.app.AlertDialog dialog = builder.create();
            Window window = dialog.getWindow();
//                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 系统级别对话框，跳到哪里都显示
            //此处可以设置dialog显示的位置
            window.setGravity(Gravity.CENTER);
            //添加动画
            window.setWindowAnimations(R.style.mystyle_2);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            AVchat.setForceLoginAvchatActivity(false);
        }
        /*if (anchorSmallImage1.getVisibility() == View.VISIBLE) anchorSmallImage1.startAnimation(animation_x);
        if (anchorSmallImage2.getVisibility() == View.VISIBLE) anchorSmallImage2.startAnimation(animation_x);
        if (anchorSmallImage3.getVisibility() == View.VISIBLE) anchorSmallImage3.startAnimation(animation_x);
        if (anchorSmallImage4.getVisibility() == View.VISIBLE) anchorSmallImage4.startAnimation(animation_x);
        if (anchorSmallImage5.getVisibility() == View.VISIBLE) anchorSmallImage5.startAnimation(animation_x);*/
    }

    //回退按钮
    public RelativeLayout anchorExit;
    //发语音按钮
    public LinearLayout call_btn;
    //视频聊天按钮
    public LinearLayout live_btn;
    //主播头像
    public CircleImageView IVanchorHeader;
    //主播名字
    public TextView TVanchorName;
    //主播年龄
    public TextView TVanchorAge;
    //主播地理位置
    public TextView TVanchorLocation;
    //主播签名
    public TextView TVanchorSign;
    //主播ID号
    public TextView TVanchorIdnum;
    public TextView TVanchorId;
    //每个主播
    private static UserID userID;
    //主播大图片
    public ImageView ivBigImage;
    //主播小图片
    public RoundImageView anchorSmallImage1,
            anchorSmallImage2,
            anchorSmallImage3,
            anchorSmallImage4,
            anchorSmallImage5;
    //主播小图片数组
    public String[] smallImage = new String[5];
    public LinearLayout anmire_btn_5_20;
    public LinearLayout anmire_btn_13_14;
    public LinearLayout anmire_btn_52_0;
    public LinearLayout anmire_btn_131_4;
    public LinearLayout anmire_btn_520;
    public LinearLayout anmire_layout;
    private Animation animation_x;
    //关注
    public LinearLayout followLayout;
    //已关注
    public LinearLayout followedLayout;
    //地理位置布局
    public LinearLayout locationLayout;
    public RelativeLayout editlayout;
    //判断点击进来的是主播还是自己
    public boolean flag;
    MediaPlayer mediaPlayer;
    //判断当前主播是否关注过
    public boolean isFollowAnchor;
    //主播的ID号
    public int anchorId;
    //自己进去头像地址
    private static String face_url = "";
    private AVchat aVchat;
    //性别
    private ImageView sex_type;
    //个性标签
    private TextView follow_anchor_label_1, follow_anchor_label_2, follow_anchor_label_3;
    //个性标签布局
    private RelativeLayout follow_label_layout1,follow_label_layout2,follow_label_layout3;
    //语音URL
    private static String voiceURL = "";
    //当前金额
    private long currentmoney = 0;

    private LinearLayout detail_anchor_image_layout;

    private Timer timer;
    private boolean isMe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //状态栏透明
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_anchordetails);
        //基本布局
        initView();
        aVchat = new AVchat();
        editlayout.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent == null) return;
        flag = intent.getBooleanExtra("flag",false);
        //true-----都是看别人资料 false----都是自己的资料
        if(flag){
            userID = (UserID) intent.getSerializableExtra("user_info");
            anchorId = userID.getAnchorId();
            voiceURL = userID.getVoiceUrl();
            isMe = false;
            ConstString.updateUserData();
            if (!TextUtils.isEmpty(ConstString.user)) {
                JSONObject jsonObject = JSON.parseObject(ConstString.user);
                if (jsonObject != null) {
                    //主播没有该字段，用户有
                    ConstString.isLiver = !jsonObject.containsKey("uType");
                    if (ConstString.isLiver) {
                        String data = jsonObject.getString("anchorId");
                        ConstString.anchor_id = (data != null && !data.trim().equals("")) ? data.trim() : "";
                        if (ConstString.anchor_id.equals(anchorId + "")) {
                            isMe = true;
                        }
                    }
                }
            }
            if (isMe) {
                initMeData();
            } else {
                queryMeandAnchorRelation();
                initLiverData();
            }
            //自动播放主播语音
            mediaPlayer = new MediaPlayer();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        if (voiceURL != null && !voiceURL.equals("")) {
                            palyVoice(voiceURL);
                        } else {
                            timer.cancel();
                        }
                    }
                }
            }, 0, 10000);
        }else {
            //editlayout.setVisibility(View.VISIBLE);
            //initMeData();
        }
    }

    //自动播放主播语音
    private void palyVoice(String voiceURL) {
        if (voiceURL != null && !voiceURL.equals("")) {
            String path = BecomeAnchorActivity.getSDPath() + ConstString.PATH_AUDIO + File.separator + anchorId + ".mp3";
            File file = new File(path);
            if (file != null && file.exists()) {
                //Log.d("m", "------------------------文件已存在--------------------------");
                hander.sendEmptyMessage(VOICE);
                return;
            }
            HttpDownloader httpDownloader1 = new HttpDownloader();
            //返回-1下载文件失败，返回0下载成功，返回1则是文件已存在
            int result = httpDownloader1.downFile(voiceURL,ConstString.PATH_AUDIO + File.separator, anchorId + ".mp3");
            switch (result) {
                case -1:
                    //Log.d("m", "返回-1下载文件失败-------------------------------------");
                    break;
                case 0:
                    //Log.d("m", "返回0下载成功--------------------------------------------");
                    hander.sendEmptyMessage(VOICE);
                    break;
                case 1:
                    //Log.d("m", "返回1则是文件已存在-----------------------------------------");
                    hander.sendEmptyMessage(VOICE);
                    break;
            }
        }
    }

    private void queryMeandAnchorRelation() {
        ConstString.updateUserData();
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if(jsonObject == null) return;
        if(ConstString.isLiver){
            String anchorId = jsonObject.getString("anchorId");
            queryfollowanchor(anchorId);
        }else {
            String userId = jsonObject.getString("userId");
            queryfollowanchor(userId);
        }
    }
    private void queryfollowanchor(String Id){
        Map<String, String> params = new HashMap<>();
        params.put("userId", Id);
        params.put("anchorId",String.valueOf(anchorId));
        params.put("time",String.valueOf(System.currentTimeMillis()));
        //请求是否关注过主播
        HttpUtil.isFollowAnchor(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(s != null && !s.equals("")){
                    JSONObject jsobkect = JSON.parseObject(s);
                    if(jsobkect == null) return;
                    isFollowAnchor = jsobkect.getBooleanValue("values");
                    if(isFollowAnchor){
                        followLayout.setVisibility(View.GONE);
                        followedLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                isFollowAnchor = false;
            }
        },params);
    }

    Animation shake;
    boolean isShake = true;

    //基本布局
    private void initView() {
        animation_x = AnimationUtils.loadAnimation(context, R.anim.en);
        //返回按钮
        anchorExit = (RelativeLayout) findViewById(R.id.anchor_details_exit_layout);
        //编辑布局
        editlayout = (RelativeLayout) findViewById(R.id.detail_anchor_edit_data);
        //主播头像
        IVanchorHeader = (CircleImageView) findViewById(R.id.detail_anchor_header_image);
        //主播名字
        TVanchorName = (TextView) findViewById(R.id.detail_anchor_name);
        //主播年龄
        TVanchorAge = (TextView) findViewById(R.id.detail_anchor_age);
        //主播地理位置布局
        locationLayout = (LinearLayout) findViewById(R.id.detail_anchor_location_layout);
        //主播地理位置
        TVanchorLocation = (TextView) findViewById(R.id.detail_anchor_location);
        //主播签名
        TVanchorSign = (TextView) findViewById(R.id.detail_anchor_sign);
        //主播ID号
        TVanchorIdnum = (TextView) findViewById(R.id.detail_anchor_id_num);
        TVanchorId = (TextView) findViewById(R.id.detail_anchor_Id);
        shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        //发起语音请求
        call_btn = (LinearLayout) findViewById(R.id.anchor_call_btn_layout);
        //call_btn.startAnimation(shake);
        //发起视频请求
        live_btn = (LinearLayout) findViewById(R.id.detail_anchor_live_btn);
        //live_btn.startAnimation(shake);
        //赞赏5.20
        anmire_btn_5_20 = (LinearLayout) findViewById(R.id.anmire_memony_one);
        //赞赏13.14
        anmire_btn_13_14 = (LinearLayout) findViewById(R.id.anmire_memony_two);
        //赞赏52.0
        anmire_btn_52_0 = (LinearLayout) findViewById(R.id.anmire_memony_three);
        //赞赏131.4
        anmire_btn_131_4 = (LinearLayout) findViewById(R.id.anmire_memony_four);
        //赞赏520
        anmire_btn_520 = (LinearLayout) findViewById(R.id.anmire_memony_five);
        //赞赏布局
        anmire_layout = (LinearLayout) findViewById(R.id.detail_anchor_admire_layout);
        //关注布局
        followLayout = (LinearLayout) findViewById(R.id.anchor_follow_btn_layout);
        //已关注布局
        followedLayout = (LinearLayout) findViewById(R.id.anchor_follow_off_btn_layout);
        //主播更换大图片
        ivBigImage = (ImageView) findViewById(R.id.anchor_image_big_image);
        //主播小图片
        anchorSmallImage1 = (RoundImageView) findViewById(R.id.detail_anchor_small_image1);
        anchorSmallImage1.setVisibility(View.INVISIBLE);
        anchorSmallImage1.setOnClickListener(this);
        anchorSmallImage2 = (RoundImageView) findViewById(R.id.detail_anchor_small_image2);
        anchorSmallImage2.setVisibility(View.INVISIBLE);
        anchorSmallImage2.setOnClickListener(this);
        anchorSmallImage3 = (RoundImageView) findViewById(R.id.detail_anchor_small_image3);
        anchorSmallImage3.setVisibility(View.INVISIBLE);
        anchorSmallImage3.setOnClickListener(this);
        anchorSmallImage4 = (RoundImageView) findViewById(R.id.detail_anchor_small_image4);
        anchorSmallImage4.setVisibility(View.INVISIBLE);
        anchorSmallImage4.setOnClickListener(this);
        anchorSmallImage5 = (RoundImageView) findViewById(R.id.detail_anchor_small_image5);
        anchorSmallImage5.setVisibility(View.INVISIBLE);
        anchorSmallImage5.setOnClickListener(this);
        anchorExit.setOnClickListener(this);
        sex_type = (ImageView) findViewById(R.id.sex_type);
        follow_anchor_label_1 = (TextView) findViewById(R.id.follow_anchor_label_1);
        follow_anchor_label_2 = (TextView) findViewById(R.id.follow_anchor_label_2);
        follow_anchor_label_3 = (TextView) findViewById(R.id.follow_anchor_label_3);
        follow_label_layout1 = (RelativeLayout) findViewById(R.id.follow_label_layout1);
        follow_label_layout2 = (RelativeLayout) findViewById(R.id.follow_label_layout2);
        follow_label_layout3 = (RelativeLayout) findViewById(R.id.follow_label_layout3);
        follow_label_layout1.setVisibility(View.INVISIBLE);
        follow_label_layout2.setVisibility(View.INVISIBLE);
        follow_label_layout3.setVisibility(View.INVISIBLE);
        detail_anchor_image_layout = (LinearLayout) findViewById(R.id.detail_anchor_image_layout);
        //全部动画部分
        /*SearchIDActivity.animation(500);
        LayoutAnimationController controller = new LayoutAnimationController(SearchIDActivity.set, 1);
        detail_anchor_image_layout.setLayoutAnimation(controller);*/
        /*anchorSmallImage1.startAnimation(animation_x);
        anchorSmallImage2.startAnimation(animation_x);
        anchorSmallImage3.startAnimation(animation_x);
        anchorSmallImage4.startAnimation(animation_x);
        anchorSmallImage5.startAnimation(animation_x);*/

        anmire_btn_5_20.startAnimation(animation_x);
        anmire_btn_13_14.startAnimation(animation_x);
        anmire_btn_52_0.startAnimation(animation_x);
        anmire_btn_131_4.startAnimation(animation_x);
        anmire_btn_520.startAnimation(animation_x);
        ivBigImage.startAnimation(animation_x);
        IVanchorHeader.startAnimation(animation_x);
    }

    //我 ---- 主播
    private void initMeData() {
        //呼叫隐藏
        call_btn.setVisibility(View.INVISIBLE);
        //视频隐藏
        live_btn.setVisibility(View.INVISIBLE);
        //关注布局隐藏
        followLayout.setVisibility(View.INVISIBLE);
        //赞赏布局隐藏
        anmire_layout.setVisibility(View.INVISIBLE);
        //设置主播ID字样
        TVanchorId.setText(String.valueOf("ID:"));
        ivBigImage.setOnClickListener(this);
        IVanchorHeader.setOnClickListener(this);
        TVanchorIdnum.setText(ConstString.anchor_id);
        parser(false);
    }

    private void parser(boolean f) {
        if (f) return;
        JSONObject jo = JSON.parseObject(ConstString.user);
        if (jo == null) return;
        String headImgUrl = jo.getString("headImgUrl");
        if (headImgUrl != null && !headImgUrl.equals("")) {
            ImageLoader.getInstance().displayImage(headImgUrl,IVanchorHeader);
        }
        String nickName = jo.getString("nickName");
        if (nickName != null && !nickName.equals("")) {
            TVanchorName.setText(nickName);
        } else {
            TVanchorName.setText(ConstString.mobile);
        }
        String age = jo.getString("age");
        if (age != null && !age.equals("")) {
            TVanchorAge.setText(age);
        } else {
            TVanchorAge.setText(String.valueOf("23岁"));
        }
        int SEX  = jo.getBooleanValue("sex") ? 1 : 0;
        if (SEX == 0) {//女
            sex_type.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.anchor_sex_presed));
        } else {//男
            sex_type.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.man));
        }
        String province = jo.getString("province");
        String city = jo.getString("city");
        if (province != null && !province.equals("")
                && city != null && !city.equals("")) {
        } else {
            province = "广东";
            city = "深圳";
        }
        TVanchorLocation.setText(province + "," + city);
        String signature = jo.getString("signature");
        if (signature != null && !signature.equals("")) {
            TVanchorSign.setText(signature);
        }
        String album = jo.getString("album");
        if (album != null && !album.equals("")) {
            smallImage = album.split(",");
            if (smallImage != null && smallImage[0] != null && !smallImage[0].equals("")) {
                ImageLoader.getInstance().displayImage(smallImage[0],ivBigImage);
            }
            loadingSmallimage(smallImage);
        }
        String background = jo.getString("background");
        if (background != null && !background.equals("")) {
            ImageLoader.getInstance().displayImage(headImgUrl,ivBigImage);
        }
        face_url = jo.getString("headImgUrl");
        if (face_url != null && !face_url.equals("")) {
            ImageLoader.getInstance().displayImage(headImgUrl,IVanchorHeader);
        }
        String labels = jo.getString("labels");
        if (labels != null && !labels.equals("")) {
            String[] ss = labels.split(",");
            for (int i = 0; i < ss.length; i++) {
                switch (i) {
                    case 0:
                        follow_anchor_label_1.setText(ss[i]);
                        follow_label_layout1.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        follow_anchor_label_2.setText(ss[i]);
                        follow_label_layout2.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        follow_anchor_label_3.setText(ss[i]);
                        follow_label_layout3.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }
    //主播
    private void initLiverData() {
        //在广场传过来的数据
        //头像
        ImageLoader.getInstance().displayImage(userID.getFace_url(),IVanchorHeader);
        DemoCache.setShowHeadImgUri(userID.getFace_url());
        //每个小图的url
        smallImage = userID.getAlbum().split(",");
        if (smallImage != null && !TextUtils.isEmpty(smallImage[0])) {
            ImageLoader.getInstance().displayImage(smallImage[0],ivBigImage);
        }
        //主播名字
        TVanchorName.setText(userID.getNickName());
        DemoCache.setAnchorNickName(userID.getNickName());
        //主播年龄
        if(userID.getYears() == 0){
            TVanchorAge.setText(String.valueOf("23"));
        }else {
            TVanchorAge.setText(String.valueOf(userID.getYears()));
        }
        //主播性别 false---女  true----男
        if(userID.isMan()){
            sex_type.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.man));
        }else {
            sex_type.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.anchor_sex_presed));
        }
        //主播定位信息
        if(TextUtils.isEmpty(userID.getProvince())){
            locationLayout.setVisibility(View.GONE);
        }else {
            if (userID.getProvince().contains("其他")) {
                TVanchorLocation.setText(userID.getCity());
            } else {
                TVanchorLocation.setText(userID.getProvince() + "," + userID.getCity());
            }
        }
        //主播签名
        if(TextUtils.isEmpty(userID.getSignature())){
            TVanchorSign.setText("该主播什么都没有留下");
        }else {
            TVanchorSign.setText(userID.getSignature());
        }
        //主播ID号
        TVanchorIdnum.setText(String.valueOf(userID.getAnchorId()));
        loadingSmallimage(smallImage);
        loadingLabels(userID.getLabels());
        call_btn.setOnClickListener(this);
        live_btn.setOnClickListener(this);
        IVanchorHeader.setOnClickListener(this);
        anmire_btn_5_20.setOnClickListener(this);
        anmire_btn_52_0.setOnClickListener(this);
        anmire_btn_13_14.setOnClickListener(this);
        anmire_btn_131_4.setOnClickListener(this);
        anmire_btn_520.setOnClickListener(this);
        followLayout.setOnClickListener(this);
        followedLayout.setOnClickListener(this);
    }

    private void loadingLabels(String labels) {
        if(labels == null) return;
        String[] labelss = labels.split(",");
        for (int i = 0; i < labelss.length; i++) {
            switch (i) {
                case 0:
                    if(labelss[i].equals("")) return;
                    follow_anchor_label_1.setText(labelss[i]);
                    follow_label_layout1.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    if(labelss[i].equals("")) return;
                    follow_anchor_label_2.setText(labelss[i]);
                    follow_label_layout2.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    if(labelss[i].equals("")) return;
                    follow_anchor_label_3.setText(labelss[i]);
                    follow_label_layout3.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void loadingSmallimage(String[] smallImage) {
        if(smallImage != null && smallImage.length > 0){
            for (int i = 0;i < smallImage.length;i++){
                switch (i){
                    case 0:{
                        ImageLoader.getInstance().displayImage(smallImage[0], anchorSmallImage1);
                        anchorSmallImage1.setVisibility(View.VISIBLE);
                        anchorSmallImage1.startAnimation(animation_x);
                        break;
                    }
                    case 1:{
                        ImageLoader.getInstance().displayImage(smallImage[1], anchorSmallImage2);
                        anchorSmallImage2.setVisibility(View.VISIBLE);
                        anchorSmallImage2.startAnimation(animation_x);
                        break;
                    }
                    case 2:{
                        ImageLoader.getInstance().displayImage(smallImage[2], anchorSmallImage3);
                        anchorSmallImage3.setVisibility(View.VISIBLE);
                        anchorSmallImage3.startAnimation(animation_x);
                        break;
                    }
                    case 3:{
                        ImageLoader.getInstance().displayImage(smallImage[3], anchorSmallImage4);
                        anchorSmallImage4.setVisibility(View.VISIBLE);
                        anchorSmallImage4.startAnimation(animation_x);
                        break;
                    }
                    case 4:{
                        ImageLoader.getInstance().displayImage(smallImage[4], anchorSmallImage5);
                        anchorSmallImage5.setVisibility(View.VISIBLE);
                        anchorSmallImage5.startAnimation(animation_x);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //回退
            case R.id.anchor_details_exit_layout:{
                finish();
                break;
            }
            //发起语音请求
            case R.id.anchor_call_btn_layout:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                } else {
                    //发起请求
                    DemoCache.setAnchorId("" + userID.getAnchorId());
                    aVchat.requestAudioOrAv(context, AVChatType.AUDIO, "" + userID.getAnchorId());
                }
                break;
            }
            //发起视频请求
            case R.id.detail_anchor_live_btn:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                } else {
                    //发起请求
                    DemoCache.setAnchorId("" + userID.getAnchorId());
                    aVchat.requestAudioOrAv(context, AVChatType.VIDEO, "" + userID.getAnchorId());
                }
                break;
            }
            //点击关注发起请求------关注
            case R.id.anchor_follow_btn_layout:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                //发起请求
                FollowAnchor();
                break;
            }
            //已关注发起请求----撤销关注
            case R.id.anchor_follow_off_btn_layout:{
                //发起请求
                unFollowAnchor();
                break;
            }
            //赞赏5.20
            case R.id.anmire_memony_one:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                    return;
                } else {
                    currentmoney = VoucherActivity.getBalance();
                    if(currentmoney - 520 < 0){
                        ShowToast.normalShow(context, "余额不足,请及时充值", true);
                        Intent intent = new Intent(context,VoucherActivity.class);
                        startActivity(intent);
                        return;
                    }
                    LikeDialog("赞赏","赞赏5.20元",520);
                }
                break;
            }
            //赞赏13.14
            case R.id.anmire_memony_two:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                    return;
                } else {
                    currentmoney = VoucherActivity.getBalance();
                    if(currentmoney  - 1314 < 0){
                        ShowToast.normalShow(context, "余额不足,请及时充值!", true);
                        Intent intent = new Intent(context, VoucherActivity.class);
                        startActivity(intent);
                        return;
                    }
                    LikeDialog("赞赏","赞赏13.14元",1314);
                }
                break;
            }
            //赞赏52.0
            case R.id.anmire_memony_three:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                    return;
                } else {
                    currentmoney = VoucherActivity.getBalance();
                    if(currentmoney - 5200 < 0){
                        ShowToast.normalShow(context, "余额不足,请及时充值!", true);
                        Intent intent = new Intent(context, VoucherActivity.class);
                        startActivity(intent);
                        return;
                    }
                    LikeDialog("赞赏","赞赏52.0元",5200);
                }
                break;
            }
            //赞赏131.4
            case R.id.anmire_memony_four:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                    return;
                } else {
                    currentmoney =  VoucherActivity.getBalance();
                    if(currentmoney - 13140 < 0){
                        ShowToast.normalShow(context, "余额不足,请及时充值!", true);
                        Intent intent = new Intent(context,VoucherActivity.class);
                        startActivity(intent);
                        return;
                    }
                    LikeDialog("赞赏","赞赏131.4元",13140);
                }
                break;
            }
            //赞赏520
            case R.id.anmire_memony_five:{
                if(!ConstString.isLogined){
                    toLogin();
                    return;
                }
                if (isMeLiver()) {
                    ShowToast.normalShow(context, "主播不可以对主播进行此操作!", true);
                    return;
                } else {
                    currentmoney = VoucherActivity.getBalance();
                    if(currentmoney - 52000 < 0){
                        ShowToast.normalShow(context, "余额不足,请及时充值!", true);
                        Intent intent = new Intent(context,VoucherActivity.class);
                        startActivity(intent);
                        return;
                    }
                    LikeDialog("赞赏","赞赏520元",52000);
                }
                break;
            }
            case R.id.detail_anchor_small_image1:
                BigShow(0);
                break;
            case R.id.detail_anchor_small_image2:
                BigShow(1);
                break;
            case R.id.detail_anchor_small_image3:
                BigShow(2);
                break;
            case R.id.detail_anchor_small_image4:
                BigShow(3);
                break;
            case R.id.detail_anchor_small_image5:
                BigShow(4);
                break;
            case R.id.detail_anchor_header_image:
                if (flag) showBigPicture(userID.getFace_url(), null, 0);
                break;
        }
    }

    private void toLogin() {
        new ActionSheetDialog(context)
                .builder()
                .setTitle("客官，本次操作需要登录进来后才能操作!\n您还没登录!")
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem("去登陆", ActionSheetDialog.SheetItemColor.Fence,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                startActivity(new Intent(context, WXEntryActivity.class));
                                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                                if(version  >= 5) {
                                    //此为自定义的动画效果，下面两个为系统的动画效果
                                    overridePendingTransition(R.anim.sq_en, R.anim.ex);
                                }
                                finish();
                            }
                        }).show();
    }

    private boolean isMeLiver() {
        ConstString.updateUserData();
        return ConstString.isLiver;
    }

    private void BigShow(int which) {
        ArrayList<String> urls = new ArrayList<>();
        for (String s : smallImage) if (!TextUtils.isEmpty(s)) urls.add(s);
        imageBrower(which, urls);
    }

    /**
     * 打开图片查看器
     *
     * @param position
     * @param urls2
     */
    protected void imageBrower(int position, ArrayList<String> urls2) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls2);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
        startActivity(intent);
        //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
        int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
        if(v1 >= 5) {
            //此为自定义的动画效果
            overridePendingTransition(R.anim.en, R.anim.ex);
        }
    }

    public void LikeDialog(String title, String msg, final long pay) {
        new com.socialassistant_youyuelive.commomentity.AlertDialog(context)
                .builder()
                .setTitle(title)
                .setMsg(msg)
                .setPositiveButton("赏赐", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        detailpay(pay);
                    }
                }).setNegativeButton("残忍拒绝", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    private void detailpay(final long pay) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", ConstString.userId);
        params.put("anchorId",String.valueOf(anchorId));
        params.put("pay",String.valueOf(pay));
        params.put("recordIdString",OkHttpUtil.randomNumber(System.currentTimeMillis()) + "");
        params.put("time", String.valueOf(System.currentTimeMillis()));
        HttpUtil.PayAnchor(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(!TextUtils.isEmpty(s)){
                    JSONObject jsobject = JSON.parseObject(s);
                    if(jsobject != null){
                        boolean status = jsobject.getBooleanValue("status");
                        if(status){
                            long balance = currentmoney - pay;
                            VoucherActivity.setBalance(balance);
                            ShowToast.normalShow(context, "赞赏成功!", true);
                        }/*else {
                            ShowToast.normalShow(context, "赞赏失败,请确认网络", true);
                        }*/
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ShowToast.normalShow(context, "访问服务器失败!", true);
            }
        },params);

    }

    /**
     * 取消关注主播
     */
    private void unFollowAnchor() {
        ConstString.updateUserData();
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if(jsonObject == null) return;
        if(ConstString.isLiver){
            String anchorId = jsonObject.getString("anchorId");
            unfollowAnchor(anchorId);
        }else {
            String userId = jsonObject.getString("userId");
            unfollowAnchor(userId);
        }
    }

    private void unfollowAnchor(String Id){
        Map<String, String> params = new HashMap<>();
        params.put("userId", Id);
        params.put("anchorId",String.valueOf(anchorId));
        params.put("type","-1");
        params.put("time",String.valueOf(System.currentTimeMillis()));
        HttpUtil.unbindAnchor(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(s != null && !s.equals("")){
                    JSONObject jsobject = JSON.parseObject(s);
                    boolean status = jsobject.getBooleanValue("status");
                    if(status){
                        followedLayout.setVisibility(View.GONE);
                        followLayout.setVisibility(View.VISIBLE);
                    }else {
                        ShowToast.normalShow(context, "取消关注失败", true);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        },params);
    }

    /**
     * 关注主播
     */
    private void FollowAnchor() {
        ConstString.updateUserData();
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if(jsonObject == null) return;
        if(ConstString.isLiver){
            String anchorId = jsonObject.getString("anchorId");
            followAnchors(anchorId);
        }else {
            String userId = jsonObject.getString("userId");
            followAnchors(userId);
        }
    }

    private void followAnchors(String Id){
        Map<String, String> params = new HashMap<>();
        params.put("userId", Id);
        params.put("anchorId",String.valueOf(anchorId));
        params.put("type","1");
        params.put("time",String.valueOf(System.currentTimeMillis()));
        HttpUtil.bindAnchor(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(!TextUtils.isEmpty(s)){
                    JSONObject jsobject = JSON.parseObject(s);
                    boolean status = jsobject.getBooleanValue("status");
                    if(status){
                        followLayout.setVisibility(View.GONE);
                        followedLayout.setVisibility(View.VISIBLE);
                    }else {
                        ShowToast.normalShow(context, "关注失败", true);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ShowToast.normalShow(context, "关注失败", true);
            }
        },params);
    }

    private void showBigPicture(String url, Bitmap bitmap, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null); // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final RoundProgressBar progressbar = (RoundProgressBar) imgEntryView.findViewById(R.id.roundProgressBar);
        ImageView img = (ImageView)imgEntryView.findViewById(R.id.large_image);
        img.startAnimation(animation_x);
        if (url == null || url.equals("")) {
            img.setImageBitmap(bitmap);
            progressbar.setVisibility(View.GONE);
        } else {
            //显示图片的配置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    /*.showImageOnLoading(R.drawable.logo)
                    .showImageOnFail(R.drawable.logo)*/
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(url, img, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    //开始加载的时候执行
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    //加载失败的时候执行
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //加载成功的时候执行
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    //加载取消的时候执行
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    //在这里更新 ProgressBar的进度信息
                    //设置进度条图片的总大小
                    progressbar.setMax(total);
                    // 设置当前加载进度
                    progressbar.setProgress(current);
                    if (current == total) {
                        progressbar.setVisibility(View.GONE);
                    }
                }
            });
        }
        dialog.setView(imgEntryView); // 自定义dialog
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
        switch (i) {
            case 5:
                window.setWindowAnimations(R.style.mystyle_2);  //添加动画
                break;
            case 0:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 2:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 3:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 4:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 1:
                window.setWindowAnimations(R.style.mystyle_3);  //添加动画
                break;
        }
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                dialog.cancel();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(v1 >= 5) {
                //此为自定义的动画效果
                overridePendingTransition(R.anim.en, R.anim.ex);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
