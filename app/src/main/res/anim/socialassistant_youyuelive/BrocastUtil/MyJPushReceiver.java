package com.socialassistant_youyuelive.BrocastUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.socialassistant_youyuelive.AV.AVchat;
import com.socialassistant_youyuelive.AV.DemoCache;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.commomentity.AboutMoney;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.Logger;
import com.socialassistant_youyuelive.fragments.ListFragmentMessage;
import com.socialassistant_youyuelive.util.ShowToast;
import com.socialassistant_youyuelive.wxapi.MainActivity;

import org.litepal.crud.DataSupport;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 *
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyJPushReceiver extends BroadcastReceiver {

    private static final String TAG = "JPush";
    private Context context = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        try {
            Bundle bundle = intent.getExtras();
            Log.d("X", intent.getAction() + "-----------------------------");
            //Logger.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                Logger.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
                //send the Registration Id to your server...

            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                String ex = bundle.getString(JPushInterface.EXTRA_EXTRA);
                Logger.d("Z", "[MyReceiver] 接收到推送下来的自定义消息: " + msg);
                Log.d("Z", msg);
                //ex = {"json":"{\"nickName\":\"hahahha\",\"faceUrl\":\"\",\"id\":\"\",\"recordId\":\"456454545\",\"pay\":\"3000\",\"type\":\"1\",\"time\":\"20170524\",\"message\":\"用户hahahha赞赏了你,收入3000点\"}"}
                Log.d("Z", ex);
                addData(ex);
                /*Intent intent_ = new Intent();
                intent_.setAction(ListFragmentMessage.ACTION);
                this.context.sendBroadcast(intent);*/
                Log.d("Z", "**********************************");
                //processCustomMessage(context, bundle);

            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 用户点击打开了通知");

                //打开自定义的Activity
                Intent i = new Intent();
                i.putExtras(bundle);
                i.setAction(ConstString.MAIN_ACTION);
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);

            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

            } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
                Logger.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
            } else {
                Logger.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "----------------------------------------------------------------------");
        }
    }

    /*public void showNotification(String contentText, String contentTitle) {
        Bitmap btm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setLargeIcon(btm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //true 自己维护通知的消失
                .setAutoCancel(false)
                .build();
        //获取通知管理器对象
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }
*/
    // 打印所有的 intent extra 数据
    /*private String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    Logger.i(TAG, "This message has no Extra data");
                    continue;
                }
                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it =  json.keys();
                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" + myKey + " - " +json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Logger.e(TAG, "Get message extra JSON error!");
                }
            } else {
                sb.append("\nkeyttt:" + key + ", value:" + bundle.getString(key));
                *//*
                if (key.equals("cn.jpush.android.INBOX")) {
                    Log.d("Z", bundle.getString(key));
                    ConstString.isNewMsg = true;
                    addData(bundle.getString(key));
                    Intent intent = new Intent();
                    intent.setAction("MSG");
                    this.context.sendBroadcast(intent);
                }
                *//*
            }
        }
        return sb.toString();
    }*/

    private void addData(String s) {
        if (TextUtils.isEmpty(s)) return;
        JSONObject jsonObjects = JSON.parseObject(s);
        if (jsonObjects == null) return;
        String jsonObjectString = "";
        if (jsonObjects.containsKey("json")) jsonObjectString = jsonObjects.getString("json");
        if (TextUtils.isEmpty(jsonObjectString)) return;
        JSONObject jsonObject = JSON.parseObject(jsonObjectString);
        if (jsonObject == null) return;
        //-----------------------------------------------------------
        AboutMoney aboutMoney = new AboutMoney();
        //类型  0代表聊天的  1代表赞赏的  2代表视频语音消费的
        String type = Function(jsonObject, "type");
        aboutMoney.setType(type);
        if (!type.equals("0")) ListFragmentMessage.handler.sendEmptyMessage(1);
        //主播昵称
        String nickName = Function(jsonObject,"nickName");
        aboutMoney.setNickName(nickName);
        //主播头像url
        String faceUrl = Function(jsonObject,"faceUrl");
        aboutMoney.setFaceUrl(faceUrl);
        //主播ID
        String anchorId = Function(jsonObject,"id");
        aboutMoney.setAnchorId(anchorId);
        //订单id
        String recordId = Function(jsonObject,"recordId");
        aboutMoney.setRecordId(recordId);
        //消费金额
        String pay = Function(jsonObject,"pay");
        aboutMoney.setPay(pay);
        /*//用户消费(用户跟主播视频后的消费)
        if(type.equals("1")){
            long currentmoney = VoucherActivity.getBalance();
            long lastmoney = currentmoney - (long)(Double.valueOf(pay) * 100);
            VoucherActivity.setBalance(lastmoney);
        }*/
        //--主播收入--用户充值-------在这里去更新该账号的金额
        if(type.equals("4") || type.equals("3")){
            long goTopUpmoney = VoucherActivity.getBalance();
            long currentmoney = goTopUpmoney + (long)(Double.valueOf(pay) * 100);
            VoucherActivity.setBalance(currentmoney);
            if (AVchat.getIsAvChating()){
                // 弹出提示框
                ShowToast.normalShow(DemoCache.getContext(), nickName + " 向您赞赏" + Double.valueOf(pay) + "金币", true);
            }
        }
        /*//主播提现---在这里去更新该账号的金额
        if(type.equals("5")){
            long currentmoney = VoucherActivity.getBalance();
            long takemoney = currentmoney - (long)(Double.valueOf(pay) * 100);
            VoucherActivity.setBalance(takemoney);
        }*/
        //时间
        String time = Function(jsonObject,"time");
        aboutMoney.setTime(time);
        //类型为0的时候有这个字段  聊天的第一句话
        String message = Function(jsonObject,"message");
        aboutMoney.setMessage(message);
        //0主播，1普通用户
        String roleType = Function(jsonObject,"roleType");
        aboutMoney.setRoleType(roleType);
        int readsum = 0;
        int talksum = 0;
        aboutMoney.setReadsum(String.valueOf(readsum));
        aboutMoney.setTalksum(String.valueOf(talksum));
        aboutMoney.save();
        //再次更新数据 lxq
        AboutMoney aboutMoney1 = new AboutMoney();
        aboutMoney1.setTalksum(String.valueOf(talksum));
        aboutMoney1.updateAll("nickname = ?",nickName);
        //List<AboutMoney> aboutMoneys = DataSupport.findAll(AboutMoney.class);
        //ShowToast.normalShow(context,aboutMoneys.size() + "",true);
        //Log.d("Z", type + ":" + nickName + ":" + faceUrl + ":" + anchorId + ":" + recordId + ":" + pay + ":" + time + ":" + message + ":" + roleType);
    }

    private String Function(JSONObject jsonObject, String key) {
        if(jsonObject.containsKey(key)){
            String s = jsonObject.getString(key);
            if (TextUtils.isEmpty(s)) return "";
            return s.replace(" ", "");
        }
        return "";
    }

    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle) {
        /*
        if (MainActivity.isForeground) {
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
            if (!ExampleUtil.isEmpty(extras)) {
                try {
                    JSONObject extraJson = new JSONObject(extras);
                    if (extraJson.length() > 0) {
                        msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
                    }
                } catch (JSONException e) {

                }

            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
        }
        */
    }
}

