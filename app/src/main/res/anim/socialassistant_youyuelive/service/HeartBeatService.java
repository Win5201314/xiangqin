package com.socialassistant_youyuelive.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.socialassistant_youyuelive.AV.AVchat;
import com.socialassistant_youyuelive.AV.DemoCache;
import com.socialassistant_youyuelive.AV.network.NetworkUtil;
import com.socialassistant_youyuelive.util.OkHttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zjm on 2017/8/2.
 */

public class HeartBeatService extends Service {
    private static final String TAG = "HeartBeatService";
    private final int START = 1;
    private static boolean noNetwork;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START){
                final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
                scheduExec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // 定时发送心跳，不用理会返回结果
                        Map<String, Object> status = new HashMap<String, Object>();
                        status.put("userId", DemoCache.getAnchorId());
                        if (NetworkUtil.isNetAvailable(HeartBeatService.this)) {// 当没有网络的情况下,不请求接口
                            Log.i(TAG, "heartBeat...");
                            try {
                                OkHttpUtil.doPost(OkHttpUtil.ANCHOR_HEARTBEAT, status);
                                if (!isNetworkConnected(HeartBeatService.this)){
                                    Log.i("AVChatActivity", "" + false);
                                }
                                // 重新连上网络之后，需要重新请求在线接口
                                if (noNetwork){
                                    noNetwork = false;
                                    AVchat aVchat = new AVchat();
                                    aVchat.updateOnlineStatus();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(TAG, e.toString());
                                noNetwork = true;// 网络断开
//                            scheduExec.shutdownNow();暂时关闭让线程一直在后台运行
                            }
                        }else {
                            noNetwork = true;// 网络断开
                        }
                    }
                }, 0, 10, TimeUnit.SECONDS);
            }
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        handler.sendEmptyMessage(START);
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable() || !networkinfo.isConnected()) {
            return false;
        }

        return true;
    }
}
