package com.socialassistant_youyuelive.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import java.util.HashMap;
import java.util.Map;

public class ChangeLiveStutasActivity extends BaseActivity implements View.OnClickListener {
    private RadioGroup group;
    private RadioButton in_live ;
    private RadioButton in_unlive ;
    private TextView keep;
    private LinearLayout ly_;
    private static boolean isLiveorUnLive = false;
    private ProgressDialog progressDialog;
    private Context context;
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) progressDialog.dismiss();
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_live_stutas);
        initToolbar(R.id.toolbar, R.id.title, getString(R.string.chat_status));
        context = this;
        initView();
        in_live.setChecked(true);
        if(in_live.isChecked()) isLiveorUnLive = true;
        ConstString.updateUserData();
        if (!TextUtils.isEmpty(ConstString.user)) {
            JSONObject jsonObject = JSON.parseObject(ConstString.user);
            if (jsonObject != null) {
                if (ConstString.isLiver) {
                    String data = jsonObject.getString("anchorId");
                    ConstString.anchor_id = (data != null && !data.trim().equals("")) ? data.trim() : "";
                }
            }
        }
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        keep = (TextView) findViewById(R.id.keep);
        keep.setOnClickListener(this);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    private void initView() {
        group = (RadioGroup) findViewById(R.id.status_live);
        in_live = (RadioButton) findViewById(R.id.in_live);
        in_unlive = (RadioButton) findViewById(R.id.in_unlive);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.in_live:
                        isLiveorUnLive = true;
                        break;
                    case R.id.in_unlive:
                        isLiveorUnLive = false;
                        break;
                }
            }
        });
        ly_ = (LinearLayout) findViewById(R.id.ly_);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.en);
        ly_.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.keep :
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(getResources().getString(R.string.app_name));
                progressDialog.setMessage(getResources().getString(R.string.submit_));
                progressDialog.setCancelable(true);
                progressDialog.show();
                changeLiveStatus(isLiveorUnLive ? "1" : "2");
                break;
        }
    }

    private Map<String,String> params = null;
    private void changeLiveStatus(String s) {
        params = new HashMap<>();
        params.put("anthorId",ConstString.anchor_id);
        params.put("type",s);
        params.put("time", String.valueOf(System.currentTimeMillis()));
        HttpUtil.changeLiveStatus(new Response.Listener<String>() {
            @Override
            public void onResponse(String ss) {
                handler.sendEmptyMessage(0);
                JSONObject jsonObject = JSON.parseObject(ss);
                if (jsonObject == null) {
                    ShowToast.show(context, R.string.submit_fail, true);
                    return;
                }
                String state = jsonObject.getString("status");
                if (state != null && state.equals("true")) {
                    ShowToast.show(context, R.string.submit_success, true);
                    finish();
                } else {
                    ShowToast.show(context, R.string.submit_fail, true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.sendEmptyMessage(0);
                if (error instanceof NetworkError) {
                    ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                } else if (error instanceof ServerError) {
                    ShowToast.normalShow(context, "服务器繁忙，请稍后重试!", true);
                } else if (error instanceof AuthFailureError) {
                    ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                } else if (error instanceof NoConnectionError) {
                    ShowToast.normalShow(context, "本地网络链接异常!", true);
                } else if (error instanceof TimeoutError) {
                    ShowToast.normalShow(context, "访问服务器超时!", true);
                } else {
                    ShowToast.show(context, R.string.access_fail, true);
                }
            }
        }, params);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(v1 >= 5) {
                //此为自定义的动画效果
                overridePendingTransition(R.anim.en, R.anim.ex);
            }*/
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
