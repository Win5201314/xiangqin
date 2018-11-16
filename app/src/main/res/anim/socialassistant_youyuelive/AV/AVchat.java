package com.socialassistant_youyuelive.AV;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.socialassistant_youyuelive.AV.network.NetworkUtil;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.fragments.ListFragmentMe;
import com.socialassistant_youyuelive.service.HeartBeatService;
import com.socialassistant_youyuelive.util.OkHttpUtil;
import com.socialassistant_youyuelive.wxapi.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zjm on 2017/5/5.
 */

public class AVchat {
    private static boolean isAvChating = false;
    private static boolean forceLoginAvchatActivity = false;// AvchatActivity在账号被强制退出时回调了finish的方法，所以不能在对应的activity上建立Dialog
    private Context context;
    // constant
    private static final String TAG = "AVChatActivity";

    public void requestAudioOrAv(Context context, AVChatType avChatType, String account){
        this.context = context;
        // 需要开启视频聊天请求
        if (NetworkUtil.isNetAvailable(context)) {
            // 需要判断余额是否充足，否则退出，并弹出提示
            int perMinute = AVChatVideo.CONSUNEPEERMINUTE;
            if (avChatType.getValue() == AVChatType.AUDIO.getValue()){
                perMinute = AVChatAudio.CONSUNEPEERMINUTE;
            }
            long accountBalance = VoucherActivity.getBalance();
            long remain = (long) (accountBalance / perMinute);
            if (remain <= 0){
                // 弹出余额不足，需要在启动activity之前
                Toast.makeText(context, R.string.avchat_balance_not_enough, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, VoucherActivity.class);
                context.startActivity(intent);
                return;
            }
            if (DemoCache.getAccount() == null){
                Toast.makeText(context, R.string.avchat_not_login_yet, Toast.LENGTH_SHORT).show();
                return;
            }
            if (DemoCache.getAccount().equals(account)){
                Toast.makeText(context, R.string.avchat_call_self_wrong, Toast.LENGTH_SHORT).show();
                return;
            }
            startAudioVideoCall(context, avChatType, account);
        } else {
            Toast.makeText(DemoCache.getContext(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /************************ 音视频通话 ***********************/

    private void startAudioVideoCall(Context context, AVChatType avChatType, String account) {
        if (AVChatActivity.needFinish){
            // getAccount()需要是对方的名称
            launch(context, account, avChatType.getValue(), AVChatActivity.FROM_INTERNAL);
        }
    }

    public void launch(Context context, String account, int callType, int source) {
        AVChatActivity.needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.putExtra(AVChatActivity.KEY_ACCOUNT, account);
        intent.putExtra(AVChatActivity.KEY_IN_CALLING, false);
        intent.putExtra(AVChatActivity.KEY_CALL_TYPE, callType);// AVChatType中的常量，表示的音频还是视频，AVChatData中包含类型这个参数
        intent.putExtra(AVChatActivity.KEY_SOURCE, source);
        context.startActivity(intent);
    }

    /**
     * incoming call
     * 来电时的方法，与主动发起通话的区别
     * @param context
     */
    public void launch(Context context, AVChatData config, int source) {
        AVChatActivity.needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AVChatActivity.KEY_CALL_CONFIG, config);// AVChatData为序列化的音视频数据，与云信api进行交互
        intent.putExtra(AVChatActivity.KEY_IN_CALLING, true);
        intent.putExtra(AVChatActivity.KEY_SOURCE, source);// AVChatActivity常量，表示来源类型
        context.startActivity(intent);
    }

    public void login(final String account, String tokenId, final Context context){
        this.context = context;
        LoginInfo info = new LoginInfo(account, tokenId); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    @Override
                    public void onSuccess(LoginInfo loginInfo) {
                        Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show();
                        DemoCache.setIsLoginYunxin(true);
                        DemoCache.setAccount(account);
                        if (DemoCache.isIsLiver()){
                            updateOnlineStatus();
                            // 添加主播用户需知弹窗，可以勾选下次不再提示
//                            Intent intent = new Intent(context, HeartBeatService.class);
//                            context.startService(intent);
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean isShowTip = sp.getBoolean(context.getString(R.string.sp_no_more_tip), true);
                            if (isShowTip){
                                disconnectAppTip(context);
                            }
                        }
                        // 登入成功之后，需要监听账号是否被挤下线
                        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, true);
                    }

                    @Override
                    public void onFailed(int i) {
                        DemoCache.setIsLoginYunxin(false);
                        Toast.makeText(context, "授权失败" + i + ",请退出重启app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        DemoCache.setIsLoginYunxin(false);
                        Toast.makeText(context, "无效输入,请退出重启app", Toast.LENGTH_SHORT).show();
                    }
                    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }

    private void disconnectAppTip(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Activity activity = (Activity) context;
        final LinearLayout linearLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.nomore_tip_layout, null);
        builder.setTitle(R.string.disconnect_app_tip_title);
        builder.setView(linearLayout);
        builder.setMessage(R.string.disconnect_app_tip_message);
        builder.setPositiveButton(R.string.avchat_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckBox checkBox = (CheckBox) linearLayout.findViewById(R.id.no_more_tip);
                if (checkBox.isChecked()){
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(context.getString(R.string.sp_no_more_tip), false);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void updateOnlineStatus(){
        // 更新主播状态(在线)
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

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            Log.i(MainActivity.TAG, "userStatusObserver");
            Log.i(MainActivity.TAG, "KICKOUT || KICK_BY_OTHER_CLIENT || FORBIDDEN || PWD_ERROR");
            // 当被挤下线的时候回调这个方法，在与对方进行音视频聊天，但是在其他地方登入当前账号时，回调这个方法
            if (code.wontAutoLogin()) {

                // 获取当前正在打开的activity
                final Activity currentActivity = ActivityCollector.getCurrentActivity();
                if (currentActivity != null){
                    String activityName = currentActivity.toString();
                    Log.i(TAG, activityName);
                    if (activityName.contains("com.socialassistant_youyuelive.AV.AVChatActivity")){
                        forceLoginAvchatActivity = true;
                        currentActivity.finish();
                        return;
                    }
                    // 弹出强制退出对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
                            .setTitle("安全提示")
                            .setMessage(R.string.avchat_login_other_place)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    // 退出当前账号
                                    ListFragmentMe.logout(currentActivity, true);
                                }
                            });
                    android.app.AlertDialog dialog = builder.create();
//                    Window window = dialog.getWindow();
////                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 系统级别对话框，跳到哪里都显示
//                    //此处可以设置dialog显示的位置
//                    window.setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            }
        }
    };

    public static void setIsAvChating(boolean isAvChating) {
        AVchat.isAvChating = isAvChating;
    }

    public static boolean isForceLoginAvchatActivity() {
        return forceLoginAvchatActivity;
    }

    public static void setForceLoginAvchatActivity(boolean forceLoginAvchatActivity) {
        AVchat.forceLoginAvchatActivity = forceLoginAvchatActivity;
    }

    public static boolean getIsAvChating() {
        return AVchat.isAvChating;
    }
}
