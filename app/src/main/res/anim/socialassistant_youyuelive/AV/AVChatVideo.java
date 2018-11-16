package com.socialassistant_youyuelive.AV;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.AV.constant.CallStateEnum;
import com.socialassistant_youyuelive.AV.constant.ToggleState;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.util.OkHttpUtil;
import com.socialassistant_youyuelive.wxapi.MainActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 视频管理器， 视频界面初始化和相关管理
 */
public class AVChatVideo implements View.OnClickListener, ToggleListener{

    // data
    private Context context;
    private View root;
    private AVChatUI manager;
    //顶部控制按钮
    private View topRoot;
    private Chronometer time;
    private TextView netUnstableTV;
    //中间控制按钮
    private View middleRoot;
    private ImageView headImg;
    private TextView nickNameTV;
    private TextView notifyTV;
    private View refuse_receive;
    private ImageView receiveIm;
    private ImageView refuseIm;
    //底部控制按钮
    private View bottomRoot;
    // 底部功能按钮
    private View bottomConnectRoot;
    private ToggleView muteToggle;
    private ToggleView speakerToggle;
    private ToggleView recordToggle;
    private Button recordToggleButton;
    ImageView hangup;
//    ToggleView switchCameraToggle;
    private TextView priceTag;
    private boolean startTimeRecord = false;

    //摄像头权限提示显示
    private View permissionRoot;

    //record
    private View recordView;
    private View recordTip;
    private View poorNetworkTip;
    private View recordWarning;

    private int topRootHeight = 0;
    private int bottomRootHeight = 0;

    private AVChatUIListener listener;

    // state
    private boolean init = false;
    private boolean shouldEnableToggle = false;

    public static final int CONSUNEPEERMINUTE = 20000;
    private int holdingSeconds = 0;
    private int showBottomSeconds = 0;
    private int poorNetworkSeconds = 0;
    private boolean timeUp = false;
    private AVchatConfigFetch configFetch;
    private final String TAG = "AVChatVideo";

    public AVChatVideo(Context context, View root, AVChatUIListener listener, AVChatUI manager, AVchatConfigFetch configFetch) {
        this.context = context;
        this.root = root;
        this.listener = listener;
        this.manager = manager;
        this.configFetch = configFetch;
    }

    private void findViews() {
        if(init || root == null )
            return;
        topRoot = root.findViewById(R.id.avchat_video_top_control);
        time = (Chronometer) topRoot.findViewById(R.id.avchat_video_time);
        time.setOnChronometerTickListener(chronometerTickListener);
        netUnstableTV = (TextView) topRoot.findViewById(R.id.avchat_video_netunstable);

        middleRoot = root.findViewById(R.id.avchat_video_middle_control);
        headImg = (ImageView) middleRoot.findViewById(R.id.avchat_video_head);
        nickNameTV = (TextView) middleRoot.findViewById(R.id.avchat_video_nickname);
        priceTag = (TextView) middleRoot.findViewById(R.id.avchat_video_price_tag);
        notifyTV = (TextView) middleRoot.findViewById(R.id.avchat_video_notify);

        refuse_receive = middleRoot.findViewById(R.id.avchat_video_refuse_receive);
        receiveIm = (ImageView) refuse_receive.findViewById(R.id.receive);
        refuseIm = (ImageView) refuse_receive.findViewById(R.id.refuse);
        receiveIm.setOnClickListener(this);
        refuseIm.setOnClickListener(this);

        recordView = root.findViewById(R.id.avchat_record_layout);
        recordTip = recordView.findViewById(R.id.avchat_record_tip);
        poorNetworkTip = root.findViewById(R.id.avchat_poor_network_tip);
        recordWarning = recordView.findViewById(R.id.avchat_record_warning);

        bottomRoot = root.findViewById(R.id.avchat_video_bottom_control);
        bottomConnectRoot = bottomRoot.findViewById(R.id.video_connect_bottom);
//        switchCameraToggle = new ToggleView(bottomConnectRoot.findViewById(R.id.avchat_switch_camera), ToggleState.OFF, this);
        View mute = bottomConnectRoot.findViewById(R.id.avchat_video_mute);
        muteToggle = new ToggleView(mute, ToggleState.OFF, this);
        View speaker = bottomConnectRoot.findViewById(R.id.avchat_video_speaker);
        speakerToggle = new ToggleView(speaker, ToggleState.OFF, this);
        View record = bottomConnectRoot.findViewById(R.id.avchat_video_record);
        recordToggle = new ToggleView(record, ToggleState.OFF, this);

        // 在底部控件中找到退出按钮，并且监听点击事件
        hangup = (ImageView) bottomRoot.findViewById(R.id.avchat_video_logout);
        hangup.setOnClickListener(this);
//        recordToggle.setOnClickListener(this);
//        recordToggle.setEnabled(false);

        permissionRoot = root.findViewById(R.id.avchat_video_permission_control);
        init = true;
    }

    // 对计时器进行监听
    private Chronometer.OnChronometerTickListener chronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // 每隔1s回调一次，用来监听录制时间
            if (!timeUp){
                // 网络环境差计时
                if (startTimeRecord){
                    poorNetworkSeconds++;
                    Log.i(TAG, "" + poorNetworkSeconds);
                    if (poorNetworkSeconds == 15){
                        setPoorNetworkTip(true);
                    }
                }
                // 只在控件显示的时候计时
                if (bottomConnectRoot.getVisibility() == View.VISIBLE){
                    showBottomSeconds++;
                    if (showBottomSeconds == 3){// 累积达到5s自动隐藏
                        setBottomConnectRoot(false);
                        setHangup(false);
                    }
                }else {
                    showBottomSeconds = 0;
                }
                holdingSeconds++;
                if (!configFetch.getIsIncoming()){
                    // 为了保证余额的准确性，需要隔一段时间与服务器进行校正，防止其他方式的恶意篡改（每隔5分钟）
                    if (holdingSeconds % 300 == 0){
                        new Thread(){
                            @Override
                            public void run() {
                                checkBalance();
                            }
                        }.start();
                    }
                    if (holdingSeconds % 60 == 0){
                        int minutes = holdingSeconds / 60;
                        long accountBalance = configFetch.getAccountBalance();
                        long remain = (accountBalance - minutes * CONSUNEPEERMINUTE) / CONSUNEPEERMINUTE;
                        if (remain <= 0){
                            timeUp = true;
                            Toast.makeText(DemoCache.getContext(), R.string.avchat_balance_not_enough, Toast.LENGTH_SHORT).show();
                            // 时间已经用完，需要结束
                            listener.onHangUp(AVChatExitCode.HANGUP);
                        }
                    }
                }
            }
        }
    };

    /**
     * 音视频状态变化及界面刷新
     * @param state
     */
    public void onCallStateChange(CallStateEnum state) {
        if(CallStateEnum.isVideoMode(state))
            findViews();
        switch (state){
            case OUTGOING_VIDEO_CALLING:// 通过拼凑控件来显示UI视图
                showProfile();//设置对方的详细信息，包括图片和昵称
                showNotify(R.string.avchat_wait_recieve);// 设置昵称下面通知的控件
                setRefuseReceive(true, true);// 设置开启和拒绝按钮的设置 // 同时要设置挂断按钮
                shouldEnableToggle = true;
                setTopRoot(false); // 设置音视频切换、时间戳和小图像，连接成功时才会显示
                setMiddleRoot(true);// 设置的是对方账号昵称和头像显示
                setBottomRoot(false);// 设置底部的一排视频选项显示 // 将挂断按钮移到挂断和接收布局中
                break;
            case INCOMING_VIDEO_CALLING:
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_video_call_request);
                setRefuseReceive(true, false); // 设置显示“接听”和“拒绝”按钮
                setPriceTag(false);// 设置标签隐藏
                setTopRoot(false);
                setMiddleRoot(true);
                setBottomRoot(false);
                break;
            case VIDEO:
                setTime(true);
                setTopRoot(true);
                setMiddleRoot(false);
                setBottomRoot(true);
                setRefuseReceive(false, false);
                setBottomConnectRoot(true);
//                enableToggle();// 设置底部按钮可点击
                showNoneCameraPermissionView(false);
                break;
            case VIDEO_CONNECTING:
                showNotify(R.string.avchat_connecting);// 页面还未跳转
                shouldEnableToggle = true;
                break;
            default:
                break;
        }
        setRoot(CallStateEnum.isVideoMode(state));
    }

    /********************** 界面显示 **********************************/

    /**
     * 显示个人信息
     */
    private void showProfile(){
        String account = manager.getNickName();
        if (account == null){
            account = manager.getAccount();
        }
        // 显示默认图片
        headImg.setImageResource(R.mipmap.nim_avatar_default);
        // 更新图片
        updateHeadImg(headImg);
        nickNameTV.setText(account);
        if (configFetch.getIsVIP()){
            priceTag.setText("会员 1.6金币/分钟");
        }
    }

    // 更新头像
    private void updateHeadImg(ImageView imageView){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        // 直接获取URI
        String headImgUri = DemoCache.getShowHeadImgUri();
        if (headImgUri != null && !headImgUri.equals("")){
            ImageLoader.getInstance().displayImage(headImgUri, imageView, options);
        }
    }

    /**
     * 显示通知
     * @param resId
     */
    private void showNotify(int resId){
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    /************************ 布局显隐设置 ****************************/

    private void setRoot(boolean visible) {
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setRefuseReceive(boolean visible, boolean refuseOrReceive){
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible){
            if (refuseOrReceive){
                refuseIm.setVisibility(View.VISIBLE);
                receiveIm.setVisibility(View.GONE);
            }else {
                refuseIm.setVisibility(View.GONE);
                receiveIm.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setTopRoot(boolean visible){
        topRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(topRootHeight == 0){
            Rect rect = new Rect();
            topRoot.getGlobalVisibleRect(rect);
            topRootHeight = rect.bottom;
        }
    }

    private void setMiddleRoot(boolean visible){
        middleRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setBottomRoot(boolean visible){
        bottomRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(bottomRootHeight == 0){
            bottomRootHeight = bottomRoot.getHeight();
        }
    }

    private void setTime(boolean visible){
        time.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(visible){
            time.setBase(manager.getTimeBase());
            time.start();
        }
    }

    private void setPriceTag(boolean visible){
        priceTag.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setHangup(boolean visible) {
        hangup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setBottomConnectRoot(boolean visible) {
        bottomConnectRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setPoorNetworkTip(boolean visible){
//        if ((recordView.getVisibility() != View.VISIBLE) && visible){
//            recordView.setVisibility(View.VISIBLE);
//        }
        poorNetworkTip.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setTimeRecord(boolean start){
        if (start){
            startTimeRecord = true;
            poorNetworkSeconds = 0;
        }else {
            setPoorNetworkTip(false);
            startTimeRecord = false;
        }
    }

    // 设置底部控件显示反转(原来显示的消失，消失的显示)
    public void changeBottomVisibility(){
        int visibility = bottomConnectRoot.getVisibility();
        switch (visibility){
            case View.VISIBLE:
                bottomConnectRoot.setVisibility(View.GONE);
                hangup.setVisibility(View.GONE);
                break;
            case View.GONE:
                bottomConnectRoot.setVisibility(View.VISIBLE);
                hangup.setVisibility(View.VISIBLE);
                break;
            case View.INVISIBLE:
                bottomConnectRoot.setVisibility(View.VISIBLE);
                hangup.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

//    /**
//     * 底部控制开关可用
//     */
//    private void enableToggle() {
//        if (shouldEnableToggle) {
////            speakerToggle.enable();
//            muteToggle.enable();
//            recordToggle.setEnabled(true);
//            shouldEnableToggle = false;
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avchat_video_logout:
            case R.id.refuse:
                listener.onHangUp(AVChatExitCode.HANGUP);
                break;
            case R.id.receive:
                listener.onReceive();
                break;
            case R.id.avchat_video_mute:
                listener.toggleMute();
                break;
            case R.id.avchat_video_speaker:
                listener.toggleSilence();
                break;
            case R.id.avchat_video_record:
                listener.toggleRecord();
                break;
//            case R.id.avchat_switch_camera:
//                listener.switchCamera();
//                break;
            default:
                break;
        }

    }

    public void showRecordView(boolean show, boolean warning) {
        if(show) {
//            recordToggle.setEnabled(true);
//            recordToggle.setSelected(true);
//            recordToggleButton.setText("结束");
            recordView.setVisibility(View.VISIBLE);
            recordTip.setVisibility(View.VISIBLE);
            if(warning) {
                recordWarning.setVisibility(View.VISIBLE);
            } else {
                recordWarning.setVisibility(View.GONE);
            }
        } else {
//            recordToggle.setSelected(false);
//            recordToggleButton.setText("录制");
            recordView.setVisibility(View.INVISIBLE);
            recordTip.setVisibility(View.INVISIBLE);
            recordWarning.setVisibility(View.GONE);
        }
    }

    public void showNoneCameraPermissionView(boolean show) {
        permissionRoot.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /******************************* toggle listener *************************/
    @Override
    public void toggleOn(View v) {
        onClick(v);
    }

    @Override
    public void toggleOff(View v) {
        onClick(v);
    }

    @Override
    public void toggleDisable(View v) {

    }

    public void closeSession(int exitCode){
        if(init){
            time.stop();
            if (exitCode == AVChatExitCode.HANGUP && holdingSeconds > 0){
                Log.i("AVChatUI", holdingSeconds + "s");
                // 计算结束时间，并且生成订单（主播订单和用户订单）
                int minutes = calculate(holdingSeconds);
                configFetch.setSeconds(holdingSeconds);
                configFetch.setMinutes(minutes);
                // 加入时间戳
                Calendar calendar = Calendar.getInstance();
                configFetch.setTimeStamp(calendar.getTimeInMillis());
                //  加入消费者和主播
                String consumer;
                String anchor;
                if (configFetch.getIsIncoming()){
                    consumer = manager.getAccount();
                    anchor = DemoCache.getAccount();
                }else {
                    consumer = DemoCache.getAccount();
                    anchor = manager.getAccount();
                }
                configFetch.setConsumer(consumer);
                configFetch.setAnchor(anchor);
                holdingSeconds = 0;
            }
            muteToggle.disable(false);
            speakerToggle.disable(false);
//            recordToggle.setEnabled(false);
//            recordToggle.disable(false);// 必须要有父控件？
            receiveIm.setEnabled(false);
            hangup.setEnabled(false);
        }
    }

    /**
     * 计算余额 //(不足1分钟，按照1分钟计算)
     */
    private int calculate(int seconds){
        int minutes = seconds / 60;
//        if ((seconds % 60) > 0){
//            minutes++;
//        }
        return minutes;
    }

    // 校验余额
    private void checkBalance(){
        String userId = DemoCache.getAccountId();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        String result = null;
        try {
            result = OkHttpUtil.doPost(OkHttpUtil.REQUEST_BALANCE, params);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
            result = null;
        }
        if (result != null){
            Log.i(TAG, result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getBoolean("status")){
                long server_balance = jsonObject.getLongValue("values");
                // 如果出现app上的金额大于服务器上的金额，则认为有异常
                if (VoucherActivity.getBalance() > server_balance){
                    timeUp = true;
                    // 同时禁止使用音视频
                    VoucherActivity.setBalance(0);
                    listener.onHangUp(AVChatExitCode.HANGUP);
                }
            }
        }
    }
}
