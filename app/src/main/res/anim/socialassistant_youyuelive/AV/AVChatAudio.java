package com.socialassistant_youyuelive.AV;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.AV.constant.CallStateEnum;
import com.socialassistant_youyuelive.AV.constant.ToggleState;
import com.socialassistant_youyuelive.AV.network.NetworkUtil;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.util.OkHttpUtil;
import com.socialassistant_youyuelive.wxapi.MainActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频管理器， 音频界面初始化和管理
 * Created by hzxuwen on 2015/4/24.
 */
public class AVChatAudio implements View.OnClickListener, ToggleListener{
    // constant
    private static final int[] NETWORK_GRADE_DRAWABLE = new int[]{R.mipmap.network_grade_0,R.mipmap.network_grade_1,R.mipmap.network_grade_2,R.mipmap.network_grade_3};
    private static final int[] NETWORK_GRADE_LABEL = new int[]{R.string.avchat_network_grade_0,R.string.avchat_network_grade_1,R.string.avchat_network_grade_2,R.string.avchat_network_grade_3};

    // view
    private View rootView ;
    //    private View switchVideo;
    private ImageView headImg;
    private TextView nickNameTV;
    private Chronometer time;
    private TextView wifiUnavailableNotifyTV;
    private TextView notifyTV;// "请求视频聊天…"
    private TextView netUnstableTV;

    private View mute_speaker_hangup;
    private ToggleView muteToggle;
    private ToggleView speakerToggle;
    private ToggleView recordToggle;
    private ImageView hangup;

    private View refuse_receive;
    private ImageView receiveIm;
    private View functionView;

    //record
    private View recordView;
    private View recordTip;
    private View recordWarning;

    // data
    private AVChatUI manager;
    private AVChatUIListener listener;
    private AVchatConfigFetch configFetch;
    private TextView priceTag;
    private TextView lawNotify;

    // state
    private boolean init = false;
    public static final int CONSUNEPEERMINUTE = 10000;
    private int holdingSeconds = 0;
    private boolean timeUp = false;
    private final String TAG = "AVChatAudio";

    public AVChatAudio(View root, AVChatUIListener listener, AVChatUI manager, AVchatConfigFetch configFetch) {
        this.rootView = root;
        this.listener = listener;
        this.manager = manager;
        this.configFetch = configFetch;
    }

    /**
     * 音视频状态变化及界面刷新
     * @param state
     */
    public void onCallStateChange(CallStateEnum state){
        if(CallStateEnum.isAudioMode(state))
            findViews();
        switch (state){
            case OUTGOING_AUDIO_CALLING: //拨打出的免费通话
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_wait_recieve);
                setWifiUnavailableNotifyTV(true);
                setMuteSpeakerHangupControl(true);
                // 设置拒绝和接听按钮的显示
                setRefuseReceive(false);
                break;
            case INCOMING_AUDIO_CALLING://免费通话请求
                showProfile();//对方的详细信息
                setPriceTag(false);// 隐藏价格标签
                showNotify(R.string.avchat_audio_call_request);
                setMuteSpeakerHangupControl(false);
                setRefuseReceive(true);
                break;
            case AUDIO:
                setWifiUnavailableNotifyTV(false);
                showConnectAfter();
//                setHangup(false);
                showNetworkCondition(1);// 设置成了固定的网络状态
                showProfile();
                setTime(true);
                hideNotify();
                setMuteSpeakerHangupControl(true);// 显示包含挂断和功能按钮的Layout
                setFunctionView(true);// 显示功能布局
                setRefuseReceive(false);
                break;
            case AUDIO_CONNECTING:
                showNotify(R.string.avchat_connecting);
                break;
            default:
                break;
        }
        setRoot(CallStateEnum.isAudioMode(state));
    }

    /**
     * 界面初始化
     */
    private void findViews() {
        if(init || rootView == null){
            return;
        }
//        switchVideo = rootView.findViewById(R.id.avchat_audio_switch_video);
//        switchVideo.setOnClickListener(this);

        headImg = (ImageView) rootView.findViewById(R.id.avchat_audio_head);
        nickNameTV = (TextView) rootView.findViewById(R.id.avchat_audio_nickname);
        priceTag = (TextView) rootView.findViewById(R.id.avchat_audio_price_tag);
        lawNotify = (TextView) rootView.findViewById(R.id.audio_law_notify);
        time = (Chronometer) rootView.findViewById(R.id.avchat_audio_time);
        time.setOnChronometerTickListener(chronometerTickListener);
        wifiUnavailableNotifyTV = (TextView) rootView.findViewById(R.id.avchat_audio_wifi_unavailable);
        notifyTV = (TextView) rootView.findViewById(R.id.avchat_audio_notify);
        netUnstableTV = (TextView) rootView.findViewById(R.id.avchat_audio_netunstable);

        mute_speaker_hangup = rootView.findViewById(R.id.avchat_audio_mute_speaker_huangup);
        functionView = mute_speaker_hangup.findViewById(R.id.audio_connect_bottom);
        View mute = functionView.findViewById(R.id.avchat_audio_mute);
        muteToggle = new ToggleView(mute, ToggleState.OFF, this);
        View speaker = functionView.findViewById(R.id.avchat_audio_speaker);
        speakerToggle = new ToggleView(speaker, ToggleState.ON, this);
        View record = functionView.findViewById(R.id.avchat_audio_record);
        recordToggle = new ToggleView(record, ToggleState.OFF, this);

        hangup = (ImageView) mute_speaker_hangup.findViewById(R.id.avchat_audio_hangup);
        hangup.setOnClickListener(this);

        refuse_receive = rootView.findViewById(R.id.avchat_audio_refuse_receive);
        receiveIm = (ImageView) refuse_receive.findViewById(R.id.receive);
        receiveIm.setOnClickListener(this);

        recordView = rootView.findViewById(R.id.avchat_record_layout);
        recordTip = rootView.findViewById(R.id.avchat_record_tip);
        recordWarning = rootView.findViewById(R.id.avchat_record_warning);

        init = true;
    }

    // 对计时器进行监听
    private Chronometer.OnChronometerTickListener chronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // 每隔1s回调一次，用来监听录制时间
            if (!timeUp){
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
     * ********************************* 界面设置 *************************************
     */

    /**
     * 个人信息设置
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
            priceTag.setText("会员 0.8金币/分钟");
        }
    }

    // 更新头像
    private void updateHeadImg(ImageView imageView){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                    /*.showImageOnLoading(R.drawable.logo)
                    .showImageOnFail(R.drawable.logo)*/
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
     * 界面状态文案设置
     * @param resId 文案
     */
    private void showNotify(int resId) {
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏界面文案
     */
    private void hideNotify(){
        notifyTV.setVisibility(View.GONE);
    }

    public void showRecordView(boolean show, boolean warning) {
        if(show) {
            recordView.setVisibility(View.VISIBLE);
            recordTip.setVisibility(View.VISIBLE);
            if(warning) {
                recordWarning.setVisibility(View.VISIBLE);
            } else {
                recordWarning.setVisibility(View.GONE);
            }
        } else {
            recordView.setVisibility(View.INVISIBLE);
            recordTip.setVisibility(View.INVISIBLE);
            recordWarning.setVisibility(View.GONE);
        }
    }

    /**
     * 显示网络状态
     * @param grade
     */
    public void showNetworkCondition(int grade){
        if(grade >= 0 && grade < NETWORK_GRADE_DRAWABLE.length){
            netUnstableTV.setText(NETWORK_GRADE_LABEL[grade]);
            Drawable drawable = DemoCache.getContext().getResources().getDrawable(NETWORK_GRADE_DRAWABLE[grade]);
            if(drawable != null){
                drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                netUnstableTV.setCompoundDrawables(null,null,drawable,null);
            }
            netUnstableTV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ***************************** 布局显隐设置 ***********************************
     */

    private void setRoot(boolean visible){
        rootView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示或者隐藏是否为wifi的提示
     * @param show
     */
    private void setWifiUnavailableNotifyTV(boolean show){
        if(show && !NetworkUtil.isWifi(DemoCache.getContext())){
            wifiUnavailableNotifyTV.setVisibility(View.VISIBLE);
        }else {
            wifiUnavailableNotifyTV.setVisibility(View.GONE);
        }
    }

    /**
     * 显示或隐藏禁音，结束通话布局
     * @param visible
     */
    private void setMuteSpeakerHangupControl(boolean visible){
        mute_speaker_hangup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示或隐藏拒绝，开启布局
     * @param visible
     */
    private void setRefuseReceive(boolean visible){
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // 显示音频连接之前的界面布局
    private void showConnectAfter(){
        priceTag.setVisibility(View.GONE);
        lawNotify.setVisibility(View.GONE);
    }

    private void setPriceTag(boolean visible){
        priceTag.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setHangup(boolean visible){
        hangup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setFunctionView(boolean visible) {
        functionView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置通话时间显示
     * @param visible
     */
    private void setTime(boolean visible){
        time.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(visible){
            time.setBase(manager.getTimeBase());
            time.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avchat_audio_hangup:
                listener.onHangUp(AVChatExitCode.HANGUP);
                break;
//            case R.id.refuse:
//                listener.onRefuse();
//                break;
            case R.id.receive:
                listener.onReceive();
                break;
            case R.id.avchat_audio_mute:
                listener.toggleMute();
                break;
            case R.id.avchat_audio_speaker:
                listener.toggleSpeaker();
                break;
            case R.id.avchat_audio_record:
                listener.toggleRecord();
                break;
            default:
                break;
        }
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
            recordToggle.disable(false);
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
                    VoucherActivity.setBalance(0);
                    listener.onHangUp(AVChatExitCode.HANGUP);
                }
            }
        }
    }

}
