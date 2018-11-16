package com.socialassistant_youyuelive.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.UserData;
import com.socialassistant_youyuelive.entity.PayReturnJson;
import com.socialassistant_youyuelive.entity.WeiXinPayEntity;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.switfpass.pay.MainApplication;
import com.switfpass.pay.activity.PayPlugin;
import com.switfpass.pay.bean.RequestMsg;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoucherActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String WEIXIN_PAY_URL_WFT = ConstString.IP + "/video/record/addDepositRecordForWFT";
    private static final String WEIXIN_PAY_URL = ConstString.IP + "/video/record/addDepositRecord";
    private static final String WEIXIN_PAY_SWITCH_URL = ConstString.IP + "/video/record/addDepositRecordForAndroid";
    private static final String WX_APPID = "wx22e6a6ee7a6357cc";
    //private static final String agentId = "1";
    //微信布局
    public RelativeLayout wxLayout;
    //支付宝布局
    public RelativeLayout zfbLayout;
    //包裹着两个Radiogroup
    public RadioGroup totalGroup;
    //第一个Radiogroup
    public RadioGroup moneyGroup1;
    //第二个Radiogroup
    public RadioGroup moneyGroup2;
    //充值点数
    public RadioButton top_up_10_btn, top_up_50_btn, top_up_100_btn, top_up_200_btn, top_up_500_btn, top_up_1000_btn;
    //充值类型
    public RadioGroup wxzfbGroup;
    //微信充值
    public RadioButton wxbtn;
    //支付宝充值
    public RadioButton zfbbtn;
    //立即充值按钮
    public Button now_btn;
    public CheckBox checkBox;
    public ProgressDialog dialog = null;
    private Boolean changeGroup = false;

    //充值多少钱
    private Long howMuch = null;
    //充值的类型:1微信 2支付宝
    private int moneyType = 1;

    private Toast toast = null;

    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);
        initToolbar(R.id.toolbar, R.id.title, "充值");
        initView();
        ConstString.updateUserData();
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void initView() {
        wxLayout = (RelativeLayout) findViewById(R.id.wx_top_up_layout);
        zfbLayout = (RelativeLayout) findViewById(R.id.zfb_top_up_layout);
        totalGroup = (RadioGroup) findViewById(R.id.voucher_money_layout);
        moneyGroup1 = (RadioGroup) findViewById(R.id.voucher_money_group_1);
        moneyGroup2 = (RadioGroup) findViewById(R.id.voucher_money_group_2);
        top_up_10_btn = (RadioButton) findViewById(R.id.voucher_top_up_10);
        top_up_50_btn = (RadioButton) findViewById(R.id.voucher_top_up_50);
        top_up_100_btn = (RadioButton) findViewById(R.id.voucher_top_up_100);
        top_up_200_btn = (RadioButton) findViewById(R.id.voucher_top_up_200);
        top_up_500_btn = (RadioButton) findViewById(R.id.voucher_top_up_500);
        top_up_1000_btn = (RadioButton) findViewById(R.id.voucher_top_up_1000);
        wxzfbGroup = (RadioGroup) findViewById(R.id.voucher_wx_or_zfb_group);
        wxbtn = (RadioButton) findViewById(R.id.wx_vocher_top_up);
        zfbbtn = (RadioButton) findViewById(R.id.zfb_vocher_top_up);
        now_btn = (Button) findViewById(R.id.voucher_top_up_btn);
        checkBox = (CheckBox) findViewById(R.id.voucher_useragreement_checkbox);
        totalGroup.setOnCheckedChangeListener(this);
        moneyGroup1.setOnCheckedChangeListener(this);
        moneyGroup2.setOnCheckedChangeListener(this);
        wxzfbGroup.setOnCheckedChangeListener(this);
        wxbtn.setOnClickListener(this);
        zfbbtn.setOnClickListener(this);
        now_btn.setOnClickListener(this);
        wxLayout.setOnClickListener(this);
        zfbLayout.setOnClickListener(this);
        top_up_10_btn.setChecked(true);
        howMuch = 6L;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //立即充值
            case R.id.voucher_top_up_btn: {
                ConstString.updateUserData();
                //ConstString.userId 胜利不给赋值了,所以自己解析出userId吧!!!!!
                JSONObject Jsonobject = JSON.parseObject(ConstString.user);
                if (Jsonobject == null) return;
                ConstString.userId = Jsonobject.getString("userId");
                String agentId = Jsonobject.getString("agentId");
                if (ConstString.userId == null || ConstString.userId.equals("")) return;
                if (checkBox.isChecked()) {
                    //howMuch:微信支付or支付宝支付
                    if (howMuch != null) {
                        if (moneyType == 1) {//微信支付
                            showDialog(this);
                            getSingleHandle();
                            weixinPaySwitch(ConstString.userId, howMuch*10000, agentId);
                            //weixinPay(ConstString.userId, howMuch*10000, agentId);
                            //weixinPayByWFT(ConstString.userId, howMuch*10000, agentId);
                        } else {//支付宝支付
                            zhifubaoPay(ConstString.userId, howMuch, agentId);
                        }
                    } else {
                        showToast("请选择充值金额");
                    }
                } else {
                    showToast("请同意用户协议");
                }
                break;
            }
            //点击微信布局则选中对应radiobutton
            case R.id.wx_top_up_layout: {
                if (!wxbtn.isChecked())
                    wxbtn.setChecked(true);
                break;
            }
            //点击支付宝布局则选中对应radiobutton
            case R.id.zfb_top_up_layout: {
                if (!zfbbtn.isChecked())
                    zfbbtn.setChecked(true);
                break;
            }
        }
    }


    /**
     * 支付宝支付接口
     *
     * @param userId  充值的用户Id
     * @param howMuch 充值金额
     * @param agentId
     */
    private void zhifubaoPay(String userId, Long howMuch, String agentId) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        //两个radiogroup 如何变成一个系列-------
        if (group != null && checkedId > -1 && changeGroup == false) {
            if (group == moneyGroup1) {
                changeGroup = true;
                moneyGroup2.clearCheck();
                changeGroup = false;
            } else if (group == moneyGroup2) {
                changeGroup = true;
                moneyGroup1.clearCheck();
                changeGroup = false;
            }
        }
        switch (checkedId) {
            case R.id.voucher_top_up_10: {
                howMuch = null;
                howMuch = 6L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择50
            case R.id.voucher_top_up_50: {
                howMuch = null;
                howMuch = 18L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择100
            case R.id.voucher_top_up_100: {
                howMuch = null;
                howMuch = 68L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择200
            case R.id.voucher_top_up_200: {
                howMuch = null;
                howMuch = 88L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择500
            case R.id.voucher_top_up_500: {
                howMuch = null;
                howMuch = 108L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择1000
            case R.id.voucher_top_up_1000: {
                howMuch = null;
                howMuch = 168L;
                Log.d("当前选择充值数量为:", String.valueOf(howMuch));
                break;
            }
            //选择微信充值
            case R.id.wx_vocher_top_up: {
                moneyType = 1;
                break;
            }
            //选择支付宝充值
            case R.id.zfb_vocher_top_up: {
                moneyType = 2;
                break;
            }
        }
    }

    // 获取余额的积分值
    private static long balance = 0;

    public static long getBalance() {
        if(balance == 0){
            ConstString.updateUserData();
            JSONObject jsonObject = JSONObject.parseObject(ConstString.user);
            if(jsonObject.containsKey("balance")){
                balance = jsonObject.getLongValue("balance");
            }
        }
        return balance;
    }

    public static void setBalance(long balance) {
        VoucherActivity.balance = balance;
        // 将user中的余额重新赋值
        JSONObject jsonObject = JSONObject.parseObject(ConstString.user);
        // 重新赋值键值对，会重新进行覆盖
        jsonObject.put("balance", balance);
        // 将余额放在user里面保存起来
        ConstString.user = jsonObject.toJSONString();
        UserData userData = DataSupport.findFirst(UserData.class);
        if (userData != null) {
            userData.setUserData(ConstString.user);
            userData.save();
        }
    }

    /**
     * 微信支付
     *
     * @param userId    用户id
     * @param cashValue 充值金额
     * @param agentId   代理id
     */
    public void weixinPayByWFT(String userId, Long cashValue, String agentId) {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        params.put("userId", userId);
        params.put("cashValue", String.valueOf(cashValue));
        params.put("agentId", agentId);
        params.put("time", time);
        String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        String key = ConstString.KEY;
        //header构造请求头
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("sign", sign);
        headerParams.put("key", key);

        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("cashValue", String.valueOf(cashValue))
                .add("agentId", agentId)
                .add("time", time)
                .build();
        Request request = new Request.Builder()
                .url(WEIXIN_PAY_URL_WFT)
                .post(formBody)
                .headers(Headers.of(headerParams))
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("tag", e.getMessage());
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                PayReturnJson data = null;
                if (json != null && !json.equals("")) {
                    try{
                        data = JSON.parseObject(json, PayReturnJson.class);
                        if (data.isStatus()) {
                            if (data.getValues().getStatus().equalsIgnoreCase("0")) {
                                handler.sendEmptyMessage(1);
                                String token_id = data.getValues().getToken_id();
                                String pay_type = MainApplication.WX_APP_TYPE;
                                Looper.prepare();
                                RequestMsg msg = new RequestMsg();
                                msg.setTokenId(token_id);
                                msg.setTradeType(pay_type);
                                msg.setAppId(WX_APPID);
                                PayPlugin.unifiedAppPay(VoucherActivity.this, msg);
                                Looper.loop();
                            }else{
                                handler.sendEmptyMessage(2);
                            }
                        }else{

                            handler.sendEmptyMessage(2);
                        }
                    }catch (JSONException e){
                        Log.d("Tag",e.getMessage());
                        handler.sendEmptyMessage(4);
                    }
                }else{
                    handler.sendEmptyMessage(4);
                }
            }
        });
    }

    /**
     * 微信支付
     *
     * @param userId
     * @param cashValue
     * @param agentId
     */
    private void weixinPay(String userId, Long cashValue, String agentId) {
        //sign签名,请求头参数
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        params.put("userId", userId);
        params.put("cashValue", String.valueOf(cashValue));
        params.put("agentId", agentId);
        params.put("time", time);
        String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        String key = ConstString.KEY;
        //header构造请求头
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("sign", sign);
        headerParams.put("key", key);
        //请求预支付订单
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("cashValue", String.valueOf(cashValue))
                .add("agentId", agentId)
                .add("time", time)
                .build();
        Request request = new Request.Builder()
                .url(WEIXIN_PAY_URL)
                .headers(Headers.of(headerParams))
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String ex = e.getMessage();
                Log.d("Tag","错误原因:"+ex);
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("Tag", res);
                WeiXinPayEntity entity = null;
                try {
                    handler.sendEmptyMessage(1);
                    entity = JSON.parseObject(res, WeiXinPayEntity.class);
                    //判断统一下单接口是否调用成功；
                    if (entity.isStatus()) {
                        IWXAPI mWxApi = WXAPIFactory.createWXAPI(VoucherActivity.this, WX_APPID, true);
                        mWxApi.registerApp(WX_APPID);
                        PayReq req = new PayReq();
                        req.appId = WX_APPID;
                        // 微信开放平台审核通过的应用APPID
                        req.partnerId = entity.getValues().getPartnerid();
                        req.prepayId = entity.getValues().getPrepayid();
                        req.nonceStr = entity.getValues().getNoncestr();
                        req.timeStamp = entity.getValues().getTimestamp();
                        req.packageValue = entity.getValues().getPackageX();
                        req.sign = entity.getValues().getSign();
                        mWxApi.sendReq(req);
                    } else {
                        Log.d("Tag", "统一下单接口调用失败");
                        handler.sendEmptyMessage(2);
                    }
                } catch (JSONException e) {
                    Log.d("Tag", "json解析异常");
                    handler.sendEmptyMessage(4);
                }
            }
        });
    }

    private void weixinPaySwitch(String userId, long cashValue, String agentId) {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        params.put("userId", userId);
        params.put("cashValue", String.valueOf(cashValue));
        params.put("agentId", agentId);
        params.put("time", time);
        String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        String key = ConstString.KEY;
        //header构造请求头
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("sign", sign);
        headerParams.put("key", key);
        //请求预支付订单
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("cashValue", String.valueOf(cashValue))
                .add("agentId", agentId)
                .add("time", time)
                .build();
        Request request = new Request.Builder()
                .url(WEIXIN_PAY_SWITCH_URL)
                .headers(Headers.of(headerParams))
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String payReturn = response.body().string();
                JSONObject json = JSONObject.parseObject(payReturn);
                Boolean statue = json.getBoolean("status");
                String message = json.getString("message");
                if(statue) {
                    try{
                        if ("0".equals(message)) {
                        handler.sendEmptyMessage(0);
                        WeiXinPayEntity entity = JSON.parseObject(payReturn, WeiXinPayEntity.class);
                        IWXAPI mWxApi = WXAPIFactory.createWXAPI(VoucherActivity.this, WX_APPID, true);
                        mWxApi.registerApp(WX_APPID);
                        PayReq req = new PayReq();
                        req.appId = WX_APPID;
                        // 微信开放平台审核通过的应用APPID
                        req.partnerId = entity.getValues().getPartnerid();
                        req.prepayId = entity.getValues().getPrepayid();
                        req.nonceStr = entity.getValues().getNoncestr();
                        req.timeStamp = entity.getValues().getTimestamp();
                        req.packageValue = entity.getValues().getPackageX();
                        req.sign = entity.getValues().getSign();
                        mWxApi.sendReq(req);
                        }
                        else if ("1".equals(message)) {
                        handler.sendEmptyMessage(1);
                        PayReturnJson data = JSON.parseObject(payReturn, PayReturnJson.class);
                        String token_id = data.getValues().getToken_id();
                        String pay_type = MainApplication.WX_APP_TYPE;
                        Looper.prepare();
                        RequestMsg msg = new RequestMsg();
                        msg.setTokenId(token_id);
                        msg.setTradeType(pay_type);
                        msg.setAppId(WX_APPID);
                        PayPlugin.unifiedAppPay(VoucherActivity.this, msg);
                        }
                        else {
                            handler.sendEmptyMessage(4);
                        }
                    }catch (JSONException e){
                        handler.sendEmptyMessage(4);
                    }
                }else{
                    Message msg = handler.obtainMessage();
                    msg.obj = message;
                    msg.what = 5;
                    handler.sendMessage(msg);
                }
            }
        });
    }
    /**
     * toast提示，防止多次弹出影响用户体验
     *
     * @param content
     */
    private void showToast(String content) {
        if (content != null && !content.equals("")) {
            if (toast == null) {
                toast = Toast.makeText(VoucherActivity.this, content, Toast.LENGTH_SHORT);
            } else {
                toast.setText(content);
                toast.setDuration(Toast.LENGTH_SHORT);
            }
            toast.show();
        }
    }

    private void showDialog(Context context){
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在处理...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
    private void cancleDialog(){
        if (dialog != null){
            dialog.cancel();
        }
    }

    /**
     * 用于操作对话框处理和支付状态显示的操作
     * @return
     */
    private Handler getSingleHandle(){
        if(handler == null){
            synchronized (this){
                if(handler == null){
                    handler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            switch (msg.what){
                                case 0: {
                                    //showToast("微信支付");
                                    //统一下单接口调用成功
                                    cancleDialog();
                                    break;
                                }
                                case 1: {
                                    //showToast("威富通支付");
                                    //统一下单接口调用成功
                                    cancleDialog();
                                    break;
                                }
                                case 2: {
                                    cancleDialog();
                                    showToast("下单失败");
                                    break;
                                }
                                case 3: {
                                    cancleDialog();
                                    showToast("下单失败,请检查网络是否可用");
                                    break;
                                }
                                case 4:{
                                    cancleDialog();
                                    showToast("数据异常");
                                }
                                case 5: {
                                    cancleDialog();
                                    String message = (String) msg.obj;
                                    if(message != null && !message.equals("")){
                                        showToast(message);
                                    }else{
                                        showToast("预支付失败");
                                    }

                                }
                            }
                        }

                    };
                }
            }
        }
        return handler;
    }

}
