package com.socialassistant_youyuelive.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.entity.WithDrawEntity;
import com.socialassistant_youyuelive.fireEditText.FireworkView;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnchorWithDrawActivity extends BaseActivity implements View.OnClickListener {
    private static final String WITHDROW_URL = ConstString.IP+"/video/record/addEnchashmentRecord";
    //主播提现金额
    private EditText edWithDrawMoney;
    //微信布局---点击选中radiobtn
    private RelativeLayout wxLayout;
    //支付宝---点击选中radiobtn
    private RelativeLayout zfbLayout;
    //微信跟支付宝选择按钮
    private RadioButton wx_btn,zfb_btn;
    //立即充值按钮
    private Button quick_withdraw_btn;
    //用户同意协议-----必须勾选
    private AppCompatCheckBox usercheckbox;

    private FireworkView mFireworkView;
    private Handler handler = null;
    public ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchor_with_draw);
        initToolbar(R.id.toolbar, R.id.title, "提现");
        initView();
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

    private void initView() {
        edWithDrawMoney = (EditText) findViewById(R.id.withdraw_money_ed);
        mFireworkView = (FireworkView) findViewById(R.id.fire);
        mFireworkView.bindEditText(edWithDrawMoney);
        wxLayout = (RelativeLayout) findViewById(R.id.withdraw_wx_layout);
        zfbLayout = (RelativeLayout) findViewById(R.id.withdraw_zfb_layout);
        wx_btn = (RadioButton) findViewById(R.id.withdraw_wx_btn);
        zfb_btn = (RadioButton) findViewById(R.id.withdraw_zfb_btn);
        quick_withdraw_btn = (Button) findViewById(R.id.withdraw_money_btn);
        usercheckbox = (AppCompatCheckBox) findViewById(R.id.withdraw_useragreement_checkbox);
        wxLayout.setOnClickListener(this);
        zfbLayout.setOnClickListener(this);
        wx_btn.setChecked(true);
        wx_btn.setOnClickListener(this);
        zfb_btn.setOnClickListener(this);
        quick_withdraw_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //微信布局
            case R.id.withdraw_wx_layout: {
                if(!wx_btn.isChecked()){
                    wx_btn.setChecked(true);
                    zfb_btn.setChecked(false);
                }
                break;
            }
            //支付宝布局
            case R.id.withdraw_zfb_layout:{
                if(!zfb_btn.isChecked()){
                    zfb_btn.setChecked(true);
                    wx_btn.setChecked(false);
                }
                break;
            }
            //微信按钮
            case R.id.withdraw_wx_btn:{
                zfb_btn.setChecked(false);
                break;
            }
            //支付宝按钮
            case R.id.withdraw_zfb_btn:{
                wx_btn.setChecked(false);
                break;
            }
            //立即充值
            case R.id.withdraw_money_btn:{
                //请同意用户协议
                if(!usercheckbox.isChecked()){
                    ShowToast.normalShow(AnchorWithDrawActivity.this,"请同意用户协议！",false);
                }else{
                    String cashValue = edWithDrawMoney.getEditableText().toString();
                    final String withDrow = cashValue;
                    //输入框是否为空
                    if(TextUtils.isEmpty(cashValue)){
                        ShowToast.normalShow(AnchorWithDrawActivity.this,"请输入提现金额!",false);
                    }else{
                        if(Integer.valueOf(withDrow) > 0){
                            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                                    .setTitleText("申请提现")
                                    .setContentText("申请提现"+withDrow+"元")
                                    .setConfirmText("确定")
                                    .setCancelText("取消")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.cancel();
                                            try{
                                                ConstString.updateUserData();
                                                JSONObject jo = JSON.parseObject(ConstString.user);
                                                if (jo != null) {
                                                    ConstString.anchor_id = jo.getString("anchorId");
                                                    getSingleHandle();
                                                    showDialog(AnchorWithDrawActivity.this);
                                                    withDrow(ConstString.anchor_id,String.valueOf(withDrow+"0000"));
                                                }else{
                                                    ShowToast.normalShow(AnchorWithDrawActivity.this,"提现异常",false);
                                                }
                                            }catch (JSONException e){
                                                ShowToast.normalShow(AnchorWithDrawActivity.this,"提现异常",false);
                                            }
                                        }
                                    })
                                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.cancel();
                                        }
                                    }).show();
                        }else{
                            ShowToast.normalShow(AnchorWithDrawActivity.this,"提现金额过低",false);
                        }
                    }

                }
                break;
            }
        }
    }

    /**
     * 提现方法
     * @param anchorId
     * @param cashValue
     */
    private void withDrow(String anchorId , final String cashValue){
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        params.put("cashValue", String.valueOf(cashValue));
        params.put("anchorId", anchorId);
        params.put("time", time);
        String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        String key = ConstString.KEY;
        //header构造请求头
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("sign", sign);
        headerParams.put("key", key);

        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("anchorId", anchorId)
                .add("cashValue", cashValue)
                .add("time", time)
                .build();
        Request request = new Request.Builder()
                .url(WITHDROW_URL)
                .headers(Headers.of(headerParams))
                .post(formBody)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    String result = response.body().string();
                    WithDrawEntity entity = JSON.parseObject(result, WithDrawEntity.class);
                    if(entity.isStatus()){
                        handler.sendEmptyMessage(2);
                       /* Long currentBalance = VoucherActivity.getBalance();
                        Long withDrowNumber = Long.valueOf(cashValue);
                        if(currentBalance > withDrowNumber){
                            VoucherActivity.setBalance(currentBalance - withDrowNumber);
                        }*/
                    }else{
                        String msg = entity.getMessage();
                        Message message = handler.obtainMessage();
                        message.obj = msg;
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                }catch (JSONException e){
                    handler.sendEmptyMessage(4);
                }
            }
        });
    }
    private void showDialog(Context context){
        dialog = new ProgressDialog(context);
        dialog.setMessage("申请提现...");
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
                                case 1:{
                                    cancleDialog();
                                    ShowToast.normalShow(AnchorWithDrawActivity.this,"提现申请提交失败,检查网络是否可用",false);
                                    break;
                                }
                                case 2:{
                                    cancleDialog();
                                    //ShowToast.normalShow(AnchorWithDrawActivity.this,"提现申请提交成功,等待审核",false);
                                    Snackbar.make(quick_withdraw_btn,"提现申请提交成功,等待审核",Snackbar.LENGTH_INDEFINITE)
                                            .setAction("我知道了",new View.OnClickListener(){
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            })
                                            .show();
                                    break;
                                }
                                case 3:{
                                    cancleDialog();
                                    String message = (String) msg.obj;
                                    if(message == null || message.length() < 1){
                                        ShowToast.normalShow(AnchorWithDrawActivity.this,"提现申请提交失败",false);
                                    }else{
                                        ShowToast.normalShow(AnchorWithDrawActivity.this,message,false);
                                    }
                                    break;
                                }
                                case 4:{
                                    cancleDialog();
                                    ShowToast.normalShow(AnchorWithDrawActivity.this,"提现异常",false);
                                    break;
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
