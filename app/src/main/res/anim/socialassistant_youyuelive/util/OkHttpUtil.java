package com.socialassistant_youyuelive.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.socialassistant_youyuelive.AV.DemoCache;
import com.socialassistant_youyuelive.AV.md5.MD5;
import com.socialassistant_youyuelive.commomentity.ConstString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zjm on 2017/5/15.
 */

public class OkHttpUtil {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private static final String TAG = "OkHttpUtil";
    // 上传订单接口
    public static final String UPLOAD_ORDER_URL = ConstString.IP + "/video/record/addConsumeRecord";
    // 请求账号余额接口
    public static final String REQUEST_BALANCE = ConstString.IP + "/video/user/queryBalanceByUserId";
    // 更新主播状态的接口
    public static final String UPDATE_STATUS = ConstString.IP + "/video/anchor/updateChatStatus";
    // 主播心跳接口
    public static final String ANCHOR_HEARTBEAT = ConstString.IP + "/video/anchor/checkAnchorOnline";

    public static String doGet(String url, Map<String,Object> params) throws IOException {
        String result = "";
        if (!(url != null && params != null)){
            Log.i(TAG, "url or params not exists");
            throw new NullPointerException("url or params not exists");
        }
        String paramStr = "";
        String mapStr;
        Map<String, Object> sequenceMap = new HashMap<>();
        for (String key : params.keySet()){
            sequenceMap.put(key, params.get(key).toString());
            mapStr = "&" + key + "=" + params.get(key).toString();
            paramStr += mapStr;
        }
        String sign = "";
        if (!paramStr.equals("")) {
            // 加多两个加密验证的键值对
            sign = createSign(sequenceMap);
            paramStr = paramStr.replaceFirst("&", "?");
            url += paramStr;
        }
        Request request = new Request.Builder().addHeader("sign", sign).addHeader("key", ConstString.KEY).url(url).build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response.isSuccessful()){
            if (response.body() != null) result = response.body().string();
        }else {
            throw new IOException("Unexpected code " + response);
        }
        return result;
    }

    // 提交表单数据
    public static String doPost(String url, Map<String,Object> params) throws IOException {
        String result = "";
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        Map<String, Object> sequenceMap = new HashMap<>();
        for (String key : params.keySet()){//set集合不按顺序排列
            bodyBuilder.add(key, params.get(key).toString());
            sequenceMap.put(key, params.get(key).toString());
//            Log.i("AVChatActivity", "key: " + key + " param: " + params.get(key).toString());
        }
        // 加多两个加密验证的键值对
        String sign = createSign(sequenceMap);
//        Log.i("AVChatActivity", "sign: " + sign + " ConstString.key: " + ConstString.KEY);
        RequestBody body = bodyBuilder.build();
        Request request = new Request.Builder().addHeader("sign", sign).addHeader("key", ConstString.KEY).url(url).post(body).build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response.isSuccessful()){
            if (response.body() != null) {
                result = response.body().string();
            }
        }else {
            Log.i(TAG, "Unexpected code " + response);
            throw new IOException("Unexpected code " + response);
        }
        return result;
    }

    public static final Long randomNumber(long timeStamp) {

        int r1 = (int) (Math.random() * (899999) + 100000);
        String randomNumber = String.valueOf(timeStamp) + String.valueOf(r1);
        return Long.valueOf(randomNumber);
    }

    // 统一加上加密接口
    public static final String createSign(Map<String, Object> params){
        SortedMap<String, Object> packageParams = new TreeMap<>();
        packageParams.putAll(params);
        StringBuffer sb = new StringBuffer();
        Set<?> es = packageParams.entrySet();
        Iterator<?> it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = entry.getKey().toString();
            if (entry.getValue() == null) continue;
            String v = entry.getValue().toString();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        if (TextUtils.isEmpty(ConstString.API_KEY)) ConstString.updateUserData();
        sb.append("key=" + ConstString.API_KEY);
        String sign = MD5.getStringMD5(sb.toString()).toUpperCase();
        return sign;
    }
}
