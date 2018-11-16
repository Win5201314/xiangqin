package com.socialassistant_youyuelive.AV;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatNetworkQuality;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoFrameRate;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoQuality;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nrtc.sdk.NRtcConstants;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.effect.VideoEffectFactory;
import com.socialassistant_youyuelive.AV.UI.UI;
import com.socialassistant_youyuelive.AV.constant.CallStateEnum;
import com.socialassistant_youyuelive.AV.network.NetworkUtil;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.AnchordetailsActivity;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.db.MyDataBaseHelper;
import com.socialassistant_youyuelive.util.FileUtils;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.OkHttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// onCallEstablished() 会话连接成功
// onDisconnectServer() 从服务器断开连接, 当自己断网后超时后，会收到此通知。
// onJoinedChannel(int code, java.lang.String audioFile, java.lang.String videoFile) 与服务器连接状态的回调
// onUserJoined(String var1) 用户加入频道
// onAVRecordingCompletion(java.lang.String account, java.lang.String filePath) 用户音视频数据录制结束
// onAudioRecordingCompletion(java.lang.String filePath) 语音录制结束
// onLowStorageSpaceWarning(long availableSize) 存储空间不足警告，存储空间低于20M时开始出现警告，出现警告时请及时关闭所有的录制服务，当存储空间低于10M时会自动关闭所有的录制功能

/**
 * Created by zjm on 2017/4/24.
 */

public class AVChatActivity extends UI implements AVChatUI.AVChatListener, AVChatStateObserver, AVchatConfigFetch{
    // constant
    private static final String TAG = "AVChatActivity";
    public static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    public static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    public static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    public static final String INTENT_ACTION_AVCHAT = "INTENT_ACTION_AVCHAT";

    /**
     * 来自广播
     */
    public static final int FROM_BROADCASTRECEIVER = 0;
    /**
     * 来自发起方
     */
    public static final int FROM_INTERNAL = 1;
    /**
     * 来自通知栏
     */
    public static final int FROM_NOTIFICATION = 2;
    /**
     * 未知的入口
     */
    public static final int FROM_UNKNOWN = -1;

    // data
    private AVChatUI avChatUI; // 音视频总管理器
    private AVChatData avChatData; // config for connect video server // 来自发起方的一系列数据
    private int state; // calltype 音频或视频
    private String receiverId; // 对方的account
    private int myNetwork = 3;
    private int userNetwork = 3;

    // state
    private boolean isUserFinish = false;
    private boolean mIsInComingCall = false;// is incoming call or outgoing call // 是主动发起音视频，还是被动接收音视频
    private boolean isCallEstablished = false; // 电话是否接通
    public static boolean needFinish = true; // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
    private boolean hasOnPause = false; // 是否暂停音视频
    private boolean hangUpByUser = false; // 用户是否主动挂断
    // notification
    private AVChatNotification notifier;
    private boolean mIsVIP = false;

    // 弹出视频聊天activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AVchat.setIsAvChating(true);
        ActivityCollector.addActivity(this);
        // checkSource()判断来源类型一定是音视频，并且解析传进来的数据
        if (needFinish || !checkSource()) {// needFinish作为结束的标志，跳回到原来的activity
            finish();
            return;
        }
        // 唤醒屏幕
        wakeAndUnlock();
        if (state == AVChatType.VIDEO.getValue()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        // 获取是否会员
        ConstString.updateUserData();
        JSONObject json = JSON.parseObject(ConstString.user);
        if (json.containsKey("member")){
            int status = (int) json.get("member");
            if (status == 1){
                mIsVIP = true;
            }
        }
        // 加载根布局，所有AvchatActivity上的布局都在avchat_activity.xml上加载
        View root = LayoutInflater.from(this).inflate(R.layout.avchat_activity, null);
        setContentView(root);
        // 加载完布局之后，再来判断是主动发起还是被动接收
        boolean isInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
        setIsIncoming(isInComingCall);
        // 初始化余额(余额在音视频通话中会用到)
        long accountBalance = VoucherActivity.getBalance();
        setAccountBalance(accountBalance);
        // 音频和视频的管理器类
        // 这个类起关键作用
        // 初始化管理器类并且初始化摄像头参数
        avChatUI = new AVChatUI(this, root, this);
        // 初始化AVChatAudio、AVChatVideo、AVChatSurface三个类的实例
        if (!avChatUI.initiation(this)) {
            this.finish();
            return;
        }
        // 先判断是主动发起还是被动接收，再判断是音频还是视频
        // 加载布局，还没有进行监听
        if (mIsInComingCall) {
            // 接收好友的音视频请求
            inComingCalling();
        } else {
            // 主动给好友发送音视频请求
            outgoingCalling();
        }
        notifier = new AVChatNotification(this);
        notifier.init(receiverId != null ? receiverId : avChatData.getAccount());

        // 需要注册观察者，监听音视频状态的变化
        // 这个方法在请求接听之后很重要，状态连接成功与否和断开全部需要注册监听
        // 监听的方法全部写在AVchatActivity，用于与云信api进行交互
        registerNetCallObserver(true);

        isCallEstablished = false; // 初始化音视频状态为false
        //放到所有UI的基类里面注册，所有的UI实现onKickOut接口
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, true);
    }

    /**
     * 注册监听
     *
     * @param register
     */
    private void registerNetCallObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(this, register);// AVChatStateObserver 音视频聊天状态回调
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);// AVChatCalleeAckEvent 被叫方状态回调
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);// AVChatCommonEvent 连接成功之后，对方挂断监听？
        // 移动端（包括android和ios）
//        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
        AVChatManager.getInstance().observeTimeoutNotification(timeoutObserver, register);// AVChatTimeOutEvent 连接超时回调
    }

    Observer<Long> timeoutObserver = new Observer<Long>() {
        @Override
        public void onEvent(Long chatId) {

            AVChatData info = avChatUI.getAvChatData();
            if (info != null && info.getChatId() == chatId) {

                avChatUI.onHangUp(AVChatExitCode.PEER_NO_RESPONSE);

                // 来电超时，自己未接听
                if (mIsInComingCall) {
                    activeMissCallNotifier();
                }

                AVChatSoundPlayer.instance().stop();
            }
            Log.i(TAG, "time out PEER_NO_RESPONSE");

        }
    };

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                AVChatSoundPlayer.instance().stop();
                finish();
            }
        }
    };

    /**
     * 注册/注销网络通话对方挂断的通知，notification进行通知
     */
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            Log.i(TAG, "avChatHangUpInfo");
            // 如果是主叫方挂断，则不足一分钟要按一分钟计算
            if (mIsInComingCall && isCallEstablished) {
                hangUpByUser = true;
            }
            AVChatSoundPlayer.instance().stop();
            // 不止关闭回话这么简单
            avChatUI.onHangUp(AVChatExitCode.HANGUP);
            cancelCallingNotifier();
            // 牵扯到UI类，通知栏的通知暂时取消
//            cancelCallingNotifier();
            // 如果是incoming call主叫方挂断，那么通知栏有通知
            if (mIsInComingCall && !isCallEstablished) {
                activeMissCallNotifier();
            }
        }
    };

    /**
     * 注册/注销网络通话被叫方的响应（接听、拒绝、忙）
     */
    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {

            AVChatSoundPlayer.instance().stop();
            // 服务器返回对方的响应状态
            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {

                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);

                avChatUI.onHangUp(AVChatExitCode.PEER_BUSY);
                Log.i(TAG, "ackInfo.getEvent(): AVChatEventType.CALLEE_ACK_BUSY");
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                avChatUI.onHangUp(AVChatExitCode.REJECT);
                Log.i(TAG, "ackInfo.getEvent(): AVChatEventType.CALLEE_ACK_REJECT");
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                avChatUI.isCallEstablish.set(true);
                avChatUI.canSwitchCamera = true;
                Log.i(TAG, "ackInfo.getEvent(): AVChatEventType.CALLEE_ACK_AGREE");
            }
            Log.i(TAG, "ackInfo.getEvent()" + ackInfo.getEvent());
        }
    };

    /**
     * 判断来电还是去电
     *
     * @return
     */
    private boolean checkSource() {
        // 来源：发起方和接收方都会传进来
        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER: // incoming call
                parseIncomingIntent();
                return true;
            case FROM_INTERNAL: // outgoing call
                parseOutgoingIntent();
                if (state == AVChatType.VIDEO.getValue() || state == AVChatType.AUDIO.getValue()) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * 接听
     */
    private void inComingCalling() {
        avChatUI.inComingCalling(avChatData);
    }

    /**
     * 拨打
     */
    private void outgoingCalling() {
        if (!NetworkUtil.isNetAvailable(AVChatActivity.this)) { // 网络不可用
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // receiverId贯穿了几个类当中
        avChatUI.outGoingCalling(receiverId, AVChatType.typeOfValue(state));
    }

    /**
     * 来电参数解析
     */
    private void parseIncomingIntent() {
        avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
        state = avChatData.getChatType().getValue();
    }

    /**
     * 去电参数解析
     */
    private void parseOutgoingIntent() {
        receiverId = getIntent().getStringExtra(KEY_ACCOUNT);// 解析的是接收方账号名称（对方账号名称）
        state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
    }

    /**
     * 通知栏
     */
    private void activeCallingNotifier() {
        if (notifier != null && !isUserFinish) {
            notifier.activeCallingNotification(true);
        }
    }

    private void cancelCallingNotifier() {
        if (notifier != null) {
            notifier.activeCallingNotification(false);
        }
    }

    private void activeMissCallNotifier() {
        if (notifier != null) {
            notifier.activeMissCallNotification(true);
        }
    }

    @Override
    public void finish() {
        isUserFinish = true;
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        avChatUI.pauseVideo(); // 暂停视频聊天（用于在视频聊天过程中，APP退到后台时必须调用）
        hasOnPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activeCallingNotifier();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelCallingNotifier();
        if (hasOnPause) {
            avChatUI.resumeVideo();
            hasOnPause = false;
        }
    }

    @Override
    public void uiExit() {
        finish();
    }

    @Override
    public void onTakeSnapshotResult(String s, boolean b, String s1) {
        Log.i(TAG, "onTakeSnapshotResult: " + s + b + s1);
    }

    @Override
    public void onConnectionTypeChanged(int i) {
        Log.i(TAG, "onConnectionTypeChanged: " + i);
    }

    private String recordFilePath = "";
    @Override
    public void onAVRecordingCompletion(String account, String filePath) {
        if (filePath.equals(recordFilePath)){
            return;
        }
        if (account != null && filePath != null && filePath.length() > 0) {
            String msg = "音视频录制已结束, "+"账号："+ account +" 录制文件已保存至：" + filePath;
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "录制已结束.", Toast.LENGTH_LONG).show();
        }
        if (avChatUI != null) {
            avChatUI.resetRecordTip();
        }
        recordFilePath = filePath;
    }

    @Override
    public void onAudioRecordingCompletion(String filePath) {
        if (filePath.equals(recordFilePath)){
            return;
        }
        if (filePath != null && filePath.length() > 0) {
            String msg = "音频录制已结束, 录制文件已保存至：" + filePath;
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "录制已结束.", Toast.LENGTH_LONG).show();
        }
        if (avChatUI != null) {
            avChatUI.resetRecordTip();
        }
        recordFilePath = filePath;
    }

    @Override
    public void onLowStorageSpaceWarning(long l) {
        if(avChatUI != null) {
            avChatUI.showRecordWarning();
        }
    }

    @Override
    public void onFirstVideoFrameAvailable(String s) {
        Log.i(TAG, "onFirstVideoFrameAvailable " + s);
    }

    @Override
    public void onVideoFpsReported(String s, int i) {
        // 可以设置汇报的回调时间？
        Log.i(TAG, "onVideoFpsReported " + "account:" + s + " FPS:" + i);
    }

    private VideoEffect mVideoEffect;
    private boolean isUninitVideoEffect = false;// 是否销毁滤镜模块
    private Handler mVideoEffectHandler;// 调用egl context 在同一个线程

    @Override
    public boolean onVideoFrameFilter(AVChatVideoFrame avChatVideoFrame, boolean maybeDualInput) {
        if (avChatVideoFrame == null || (Build.VERSION.SDK_INT < 18)) {
            return true;
        }
        if (mVideoEffect == null && isUninitVideoEffect == false) {
            Log.i("onVideoFrameFilter", "create Video Effect " + maybeDualInput);
            mVideoEffectHandler = new Handler();
            mVideoEffect = VideoEffectFactory.getVCloudEffect();
            mVideoEffect.init(this, true, false);
//            mVideoEffect.setFilterType(VideoEffect.FilterType.brooklyn);
            mVideoEffect.setBeautyLevel(5);
//            mVideoEffect.setFilterLevel(0.5f);
        }
        if (mVideoEffect == null) {
            return true;
        }
        return true;
    }

    /**
     * 处理连接服务器的返回值
     *
     * @param auth_result
     */
    protected void handleWithConnectServerResult(int auth_result) {
        Log.i(TAG, "result code->" + auth_result);
        if (auth_result == 200) {
            Log.d(TAG, "onConnectServer success");
        } else if (auth_result == 101) { // 连接超时
            avChatUI.onHangUp(AVChatExitCode.PEER_NO_RESPONSE);
        } else if (auth_result == 401) { // 验证失败
            avChatUI.onHangUp(AVChatExitCode.CONFIG_ERROR);
        } else if (auth_result == 417) { // 无效的channelId
            avChatUI.onHangUp(AVChatExitCode.INVALIDE_CHANNELID);
        } else { // 连接服务器错误，直接退出
            avChatUI.onHangUp(AVChatExitCode.CONFIG_ERROR);
        }
    }

    @Override
    public void onJoinedChannel(int i, String s, String s1, int i1) {
        Log.i(TAG, "onLeaveChannel");
    }

    @Override
    public void onUserJoined(String account) {
        Log.i(TAG, "onUserJoin -> " + account);
        avChatUI.setVideoAccount(account);
        avChatUI.initLargeSurfaceView(avChatUI.getVideoAccount());
    }

    @Override
    public void onUserLeave(String account, int event) {
        Log.i(TAG, "onUserLeave -> " + account);
        // 当对方好友已离开频道，需要强行退出
        if (avChatUI.getVideoAccount().equals(account)){
            avChatUI.onHangUp(AVChatExitCode.HANGUP);
        }
    }

    @Override
    public void onLeaveChannel() {

    }

    @Override
    public void onProtocolIncompatible(int i) {
        Log.i(TAG, "onProtocolIncompatible: " + i);
    }

    @Override
    public void onDisconnectServer() {
        Log.i(TAG, "onDisconnectServer");
        //  当断网超时后，需要关闭回话
        avChatUI.onHangUp(AVChatExitCode.HANGUP);
    }

    @Override
    public void onNetworkQuality(String s, int i, AVChatNetworkStats stats) {
        // 当断网的时候，双方的状态都为"bad"
        String networkQuality = "";
        switch (i){
            case 0:
                networkQuality = "EXCELLENT";
                break;
            case 1:
                networkQuality = "GOOD";
                break;
            case 2:
                networkQuality = "POOR";
                break;
            case 3:
                networkQuality = "BAD";
                break;
            default:
                break;
        }
        if (CallStateEnum.isVideoMode(avChatUI.getCallingState())){
            AVChatParameters avChatParameters = new AVChatParameters();
            // 根据对方的网络状态，动态改变画面画质和帧率
            if (!DemoCache.getAccount().equals(s)){
                if (i == AVChatNetworkQuality.BAD){
                    // 将画质调到最低，并且调整帧率
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_LOW);
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_10);
                    avChatUI.setTimeRecord(true);
                }else if (i == AVChatNetworkQuality.POOR || i == AVChatNetworkQuality.GOOD){
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_MEDIUM);
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_15);
                    avChatUI.setTimeRecord(false);
                }else {
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_HIGH);
                    avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_15);
                    avChatUI.setTimeRecord(false);
                }
                AVChatManager.getInstance().setParameters(avChatParameters);
            }
        }
        if (DemoCache.getAccount().equals(s)){
            myNetwork = i;
        }else {
            userNetwork = i;
        }
        Log.i(TAG, "onNetworkQuality " + "user:" + s + " " + networkQuality);
//        if ((myNetwork + userNetwork) <= 2){
//            avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, NRtcConstants.VideoQuality.QUALITY_HIGH);
//            AVChatManager.getInstance().setParameters(avChatParameters);
//            Log.i(TAG, "AVChatVideoQuality.QUALITY_HIGH");
//        }
//        if (myNetwork.equals("BAD") && userNetwork.equals("BAD")){
//            avChatUI.onHangUp();
//        }
    }

    @Override
    public void onCallEstablished() {// 音视频链接成功的时候，调用此方法
        Log.i(TAG, "onCallEstablished");
        if (avChatUI.getTimeBase() == 0) {
            avChatUI.setTimeBase(SystemClock.elapsedRealtime());// 将起始时间记录下来
        }

        if (state == AVChatType.AUDIO.getValue()) {
            // 音频链接成功，重新加载页面
            avChatUI.onCallStateChange(CallStateEnum.AUDIO);
        } else {
            // 初始化头像并且将灰色背景层抽空
            avChatUI.initSmallSurfaceView();
            avChatUI.onCallStateChange(CallStateEnum.VIDEO);
        }
        isCallEstablished = true;
    }

    @Override
    public void onDeviceEvent(int i, String s) {
        Log.i(TAG, "onDeviceEvent" + i + " " + s);
    }

    @Override
    public void onFirstVideoFrameRendered(String s) {
        Log.i(TAG, "onFirstVideoFrameRendered " + s);
    }

    @Override
    public void onVideoFrameResolutionChanged(String s, int i, int i1, int i2) {
        Log.i(TAG, "onVideoFrameResolutionChanged " + s + " " + i + " " + i1 + " " + i2);
    }


    @Override
    public boolean onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
        Log.i(TAG, "onAudioFrameFilter");
        return true;
    }

    @Override
    public void onAudioDeviceChanged(int i) {
        Log.i(TAG, "onAudioDeviceChanged");
    }

    @Override
    public void onReportSpeaker(Map<String, Integer> map, int i) {
        Log.i(TAG, "onReportSpeaker: " + i);
    }

    @Override
    public void onAudioMixingEvent(int i) {
        Log.i(TAG, "onAudioMixingEvent");
    }

    @Override
    public void onSessionStats(AVChatSessionStats avChatSessionStats) {

    }

    @Override
    public void onLiveEvent(int i) {

    }

    private int seconds;
    private int minutes;
    private long timeStamp;
    private String consumer;
    private String anchor;
    private long accountBalance;

    @Override
    public int getSeconds() {
        return this.seconds;
    }

    @Override
    public int getMinutes() {
        return this.minutes;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public String getConsumer() {
        return this.consumer;
    }

    @Override
    public String getAnchor() {
        return this.anchor;
    }

    @Override
    public boolean getIsIncoming() {
        return this.mIsInComingCall;
    }

    @Override
    public long getAccountBalance() {
        return this.accountBalance;
    }

    @Override
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    @Override
    public void setIsIncoming(boolean isIncoming) {
        this.mIsInComingCall = isIncoming;
    }

    @Override
    public void setAccountBalance(long accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public boolean getIsVIP() {
        return mIsVIP;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCallingNotifier();
        AVchat.setIsAvChating(false);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, false);
        registerNetCallObserver(false);
        needFinish = true;
        ActivityCollector.removeActivity(this);
        if (state == AVChatType.VIDEO.getValue()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // 释放资源
            if (mVideoEffect != null) {
                Log.i(TAG, "mVideoEffect != null");
                isUninitVideoEffect = true;
                mVideoEffectHandler.post(new Runnable() {
                    @Override
                    public void run() {// 走不到这里
                        Log.i(TAG, "releaseRtc unInit");
                        mVideoEffect.unInit();
                        mVideoEffect = null;
                    }
                });
            }
        }
        Log.i(TAG, "onDestroy");
        if (mIsInComingCall){
            // 更新主播状态(可通话)
            final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
            scheduExec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 1.主播Id,2.状态 0忙碌中 1可通话 2是免打扰
                        Map<String, Object> status = new HashMap<String, Object>();
                        status.put("anthorId", DemoCache.getAnchorId());
                        status.put("type", 1);
                        String result = OkHttpUtil.doPost(OkHttpUtil.UPDATE_STATUS, status);
                        Log.i(TAG, "status: " + result);
                        if (!result.equals("")){
                            // 解析返回的数据并打印
                            JSONObject json = JSONObject.parseObject(result);
                            boolean isSendSuccess = json.getBoolean("status");
                            if (isSendSuccess){
                                scheduExec.shutdownNow();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        scheduExec.shutdownNow();
                    }
                }
            }, 0, 3, TimeUnit.SECONDS);
        }

        // 将余额统一规定为long类型（积分）
        long account_balance = this.accountBalance;
        int seconds = this.seconds;
        if (hangUpByUser){
            if ((seconds % 60) > 0){
                this.minutes++;
            }
        }
        int minutes = this.minutes;
        String userId = DemoCache.getAccountId();
        String anchorId = DemoCache.getAnchorId();
        if (minutes > 0){
            // 将消费之前的余额放进变量中
            long account_balance_before = account_balance;
            // 判断消费类型
            long consume = minutes * AVChatVideo.CONSUNEPEERMINUTE;
            int chatType = 1;// 0 音频 1 视频
            if (state == AVChatType.AUDIO.getValue()){
                consume = minutes * AVChatAudio.CONSUNEPEERMINUTE;
                chatType = 0;
            }
            long timeStamp = randomNumber(this.timeStamp);
            String consumer = this.consumer;
            String anchor = this.anchor;
            int isSendSuccess = -1;
            if (mIsInComingCall){// isSendSuccess -1 不用发送 0 未发送 1 已发送
                isSendSuccess = 0;// 现设置只有主播状态才发送订单信息
            }else {
                consume = -consume;
            }
            account_balance += consume;
            // 写入余额
            VoucherActivity.setBalance(account_balance);
            // 将信息写入到数据库
            MyDataBaseHelper dataBaseHelper = MyDataBaseHelper.getInstance(AVChatActivity.this);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Map<String, Object> params = new HashMap<>();
            params.put("timeStamp", timeStamp);
            params.put("consumer", consumer);
            params.put("anchor", anchor);
            params.put("seconds", seconds);
            params.put("minutes", minutes);
            params.put("account_balance_before", account_balance_before);
            params.put("consume", consume);
            params.put("account_balance", account_balance);
            params.put("userId", userId);
            params.put("anchorId", anchorId);
            Log.i(TAG, "userId: "+ userId + " anchorId: " + anchorId);
            params.put("chatType", chatType);
            params.put("isSendSuccess", isSendSuccess);
            boolean isSuccess = dataBaseHelper.insertOrderData(db, params);
            if (isSuccess){
                Log.i(TAG, "insert record into database");
            }
            if (mIsInComingCall){// 现设置只有主播状态才发送订单信息
//            // 写入到文件（文件达到一定的个数不上传，则禁用功能）
//            String filePath = FileUtils.ORDERDIRECTORY;
//            File file = new File(filePath);
//            if (!file.exists()){
//                file.mkdirs();
//            }
//            JSONObject jsonObject = new JSONObject();
//            for (String key : params.keySet()){
//                jsonObject.put(key, params.get(key));
//            }
//            FileUtils fileUtils = new FileUtils();
//            filePath = filePath + "/" + timeStamp + ".txt";
//            fileUtils.writeFileByByte(filePath, jsonObject.toJSONString());
                // 使用定时器进行周期性上传，在上传不成功的情况下
                final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
                // 先不加入次数限制，看测试结果

                scheduExec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // 查询并返回未发送的第一个订单
                        MyDataBaseHelper dataBaseHelper = MyDataBaseHelper.getInstance(AVChatActivity.this);
                        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
                        Map<String, Object> params = dataBaseHelper.getUnsendOrder(db);
//                        FileUtils fileUtils = new FileUtils();
//                        if (params == null){
//                            // 判断文件夹下是否有文件，如果有，则上传第一个文件
//                            params = fileUtils.searchOrderFile(FileUtils.ORDERDIRECTORY);
//                        }
                        if (params == null){
                            // 可能还需要加入文件的判断，防止故意卸载软件，导致数据库丢失(由主播上传订单信息，防止恶意卸载)
                            Log.i(TAG, "no order any more");
                            scheduExec.shutdownNow();
                            return;
                        }else {
                            Log.i(TAG, params.get("consumer") + " " + params.get("anchor") + " " + params.get("pay") + " " + params.get("recordIdString"));
                        }
                        // 四个字段：消费者，主播，消费金额，用来区别订单的唯一id
                        String result = "";
                        try {
                            if (NetworkUtil.isNetAvailable(AVChatActivity.this)){// 当没有网络的情况下,不请求接口

                                result = OkHttpUtil.doPost(OkHttpUtil.UPLOAD_ORDER_URL, params);
                            }
                        } catch (IOException e) {
                            result = null;
                        }
                        boolean isSendSuccess = false;
                        if (result == null){// 服务器没有做出反应，则认为服务器挂了
                            Log.i(TAG, "server shutdown");
                            scheduExec.shutdownNow();
                            return;
                        }
                        if (!result.equals("")){
                            // 解析返回的数据并打印
                            JSONObject json = JSONObject.parseObject(result);
                            isSendSuccess = json.getBoolean("status");
                            Log.i(TAG, result);
                        }
                        Log.i(TAG, "" + isSendSuccess);
                        if (isSendSuccess){
                            // 更新数据库操作
                            long timeStamp = (Long) params.get("recordIdString");
                            boolean isSuccess = dataBaseHelper.updateUnsendOrder(db, timeStamp);
                            if (isSuccess){
                                Log.i(TAG, "update timeStamp " + timeStamp);
                            }
//                            // 删除对应的文件
//                            String filePath = FileUtils.ORDERDIRECTORY;
//                            filePath = filePath + "/" + timeStamp + ".txt";
//                            fileUtils.deleteFile(filePath);
                        }
                    }
                }, 0, 5, TimeUnit.SECONDS);
            }
        }
    }

    public final static Long randomNumber(long timeStamp) {
        // 加上后6位随机数
        int r1 = (int) (Math.random() * (899999) + 100000);
        String randomNumber = String.valueOf(timeStamp) + String.valueOf(r1);
        return Long.valueOf(randomNumber);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            // 判断连接的状态
            if (isCallEstablished){
                avChatUI.onHangUp(AVChatExitCode.HANGUP);
            }else {
                avChatUI.onHangUp(AVChatExitCode.CANCEL);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点亮屏幕并解锁
     */
    private void wakeAndUnlock(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn){
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire();// 点亮屏幕

            // 解锁
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            kl.disableKeyguard();
        }
    }
}
