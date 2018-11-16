package com.socialassistant_youyuelive.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.UserData;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/22.
 */

public class ChangeSexTypeActivity extends BaseActivity implements View.OnClickListener {

    private RadioGroup group;
    private RadioButton boy ;
    private RadioButton girl ;
    private TextView keep;
    private LinearLayout ly_;
    private Context context;
    private ProgressDialog progressDialog;
    private boolean isGirl = true;
    private static final String url = ConstString.URL_INFO;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) progressDialog.dismiss();
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sex_type);
        context = this;
        initToolbar(R.id.toolbar, R.id.title, getString(R.string.change_sex));
        ly_ = (LinearLayout) findViewById(R.id.ly_);
        group = (RadioGroup) findViewById(R.id.sex);
        boy = (RadioButton) findViewById(R.id.boy);
        girl = (RadioButton) findViewById(R.id.girl);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.boy:
                        isGirl = false;
                        break;
                    case R.id.girl:
                        isGirl = true;
                        break;
                }
            }
        });
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
        ly_.startAnimation(animation);

        ConstString.updateUserData();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep:
                ConstString.updateUserData();
                if (TextUtils.isEmpty(ConstString.user)) return;
                JSONObject jo = JSON.parseObject(ConstString.user);
                if (jo != null) {
                    int SEX  = jo.getBooleanValue("sex") ? 1 : 0;
                    int sex = isGirl ? 0 : 1;
                    if (SEX == sex) {
                        AlbumActivity.ShowAlerter(ChangeSexTypeActivity.this);
                    } else {
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(getResources().getString(R.string.app_name));
                        progressDialog.setMessage(getResources().getString(R.string.submit_));
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                        changeNickName(isGirl ? "0" : "1");
                    }
                }
                break;
        }
    }

    private Map<String, String> params = new HashMap<>();
    private void changeNickName(String sex) {
        // TODO Auto-generated method stub
        params.put("sex", sex);
        if (ConstString.isLiver) {
            params.put("userId", ConstString.anchor_id);
        } else {
            params.put("userId", ConstString.userId);
        }
        params.put("time", String.valueOf(System.currentTimeMillis()));
        final String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        if (TextUtils.isEmpty(ConstString.KEY)) ConstString.updateUserData();
        final String key = ConstString.KEY;
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handler.sendEmptyMessage(0);
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) {
                            ShowToast.show(context, R.string.submit_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ShowToast.show(context, R.string.submit_success, true);
                            ConstString.user = jsonObject.getString("user");
                            UserData userData = DataSupport.findFirst(UserData.class);
                            if (userData != null) {
                                userData.setUserData(ConstString.user);
                                userData.save();
                            }
                            finish();
                        } else {
                            ShowToast.show(context, R.string.submit_fail, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
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
        }) {
            @Override
            protected Map<String, String> getParams() {
                /*Map<String, String> params = new HashMap<>();
                params.put("sex", sex);
                if (ConstString.isLiver) {
                    params.put("userId", ConstString.anchor_id);
                } else {
                    params.put("userId", ConstString.userId);
                }*/
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> pa = new HashMap<>();
                //String sign = HttpUtil.createSign(params, ConstString.API_KEY);
                pa.put("sign", sign);
                pa.put("key", key);
                return pa;
            }
        };
        mQueue.add(postRequest);
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
