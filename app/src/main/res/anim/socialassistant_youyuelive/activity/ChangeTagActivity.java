package com.socialassistant_youyuelive.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.socialassistant_youyuelive.lizi.ParticleView;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/22.
 */

public class ChangeTagActivity extends BaseActivity implements View.OnClickListener {

    private static final String url = ConstString.URL_INFO;
    private TextView keep;
    private ProgressDialog progressDialog;
    private Context context;
    //已经选择
    private TextView tag_1, tag_2, tag_3;
    //自定义标签
    private LinearLayout add_layout;
    private ImageView add_tag;
    private TextView tag_my_1, tag_my_2, tag_my_3;
    //死标签
    private TextView t_1, t_2, t_3, t_4, t_5, t_6, t_7, t_8, t_9;
    private ParticleView particleAnimator;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) progressDialog.dismiss();
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_tag);
        initToolbar(R.id.toolbar, R.id.title, getString(R.string.change_tag));
        context = this;
        initView();
        initLabels();
        particleAnimator = new ParticleView(this, 600);
        particleAnimator.setOnAnimationListener(new ParticleView.OnAnimationListener() {
            @Override
            public void onAnimationStart(View view,Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationEnd(View view,Animator animation) {
                view.setVisibility(View.VISIBLE);
            }
        });

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
                String tag = "";
                if (tag_1.getVisibility() == View.VISIBLE) tag += tag_1.getText().toString() + ",";
                if (tag_2.getVisibility() == View.VISIBLE) tag += tag_2.getText().toString() + ",";
                if (tag_3.getVisibility() == View.VISIBLE) tag += tag_3.getText().toString();
                if ((!TextUtils.isEmpty(tag) && !tag.equals(labels)) ||
                        (TextUtils.isEmpty(tag) && !TextUtils.isEmpty(labels))) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.app_name));
                    progressDialog.setMessage(getResources().getString(R.string.submit_));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    changeTAG(tag);
                } else {
                    AlbumActivity.ShowAlerter(ChangeTagActivity.this);
                }
                break;
            case R.id.add_tag:
                if (tag_1.getVisibility() == View.GONE
                        || tag_2.getVisibility() == View.GONE
                        ||tag_3.getVisibility() == View.GONE) {
                    startActivityForResult(new Intent(context, TagActivity.class), 1);
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                } else {
                    ShowToast.normalShow(context, "已选择了三个，请先去掉一个!", true);
                }
                break;
            case R.id.add_layout:
                if (tag_1.getVisibility() == View.GONE
                        || tag_2.getVisibility() == View.GONE
                        ||tag_3.getVisibility() == View.GONE) {
                    startActivityForResult(new Intent(context, TagActivity.class), 1);
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                } else {
                    ShowToast.normalShow(context, "已选择了三个，请先去掉一个!", true);
                }
                break;
            case R.id.tag_1:
                tag_1.setText("");
                tag_1.setVisibility(View.GONE);
                break;
            case R.id.tag_2:
                tag_2.setText("");
                tag_2.setVisibility(View.GONE);
                break;
            case R.id.tag_3:
                tag_3.setText("");
                tag_3.setVisibility(View.GONE);
                break;
            case R.id.t_1:
                particleAnimator.boom(v);
                addTag(t_1.getText().toString());
                break;
            case R.id.t_2:
                particleAnimator.boom(v);
                addTag(t_2.getText().toString());
                break;
            case R.id.t_3:
                particleAnimator.boom(v);
                addTag(t_3.getText().toString());
                break;
            case R.id.t_4:
                particleAnimator.boom(v);
                addTag(t_4.getText().toString());
                break;
            case R.id.t_5:
                particleAnimator.boom(v);
                addTag(t_5.getText().toString());
                break;
            case R.id.t_6:
                particleAnimator.boom(v);
                addTag(t_6.getText().toString());
                break;
            case R.id.t_7:
                particleAnimator.boom(v);
                addTag(t_7.getText().toString());
                break;
            case R.id.t_8:
                particleAnimator.boom(v);
                addTag(t_8.getText().toString());
                break;
            case R.id.t_9:
                particleAnimator.boom(v);
                addTag(t_9.getText().toString());
                break;
        }
    }

    private void addTag(String s) {
        String[] t = new String[3];
        if (tag_1.getVisibility() == View.VISIBLE) t[0] = tag_1.getText().toString();
        if (tag_2.getVisibility() == View.VISIBLE) t[1] = tag_2.getText().toString();
        if (tag_3.getVisibility() == View.VISIBLE) t[2] = tag_3.getText().toString();
        for (int i = 0; i < 3; i++) if (s.equals(t[i])) return;
        if (tag_1.getVisibility() == View.GONE) {
            tag_1.setText(s);
            tag_1.setVisibility(View.VISIBLE);
            return;
        }
        if (tag_2.getVisibility() == View.GONE) {
            tag_2.setText(s);
            tag_2.setVisibility(View.VISIBLE);
            return;
        }
        if (tag_3.getVisibility() == View.GONE) {
            tag_3.setText(s);
            tag_3.setVisibility(View.VISIBLE);
        }
    }

    private Map<String, String> params = new HashMap<>();
    private void changeTAG(String tag) {
        // TODO Auto-generated method stub
        params.put("labels", tag);
        params.put("userId", ConstString.anchor_id);
        params.put("time", String.valueOf(System.currentTimeMillis()));
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
                params.put("labels", tag);
                params.put("userId", ConstString.anchor_id);*/
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> pa = new HashMap<>();
                String sign = HttpUtil.createSign(params, ConstString.API_KEY);
                pa.put("sign", sign);
                pa.put("key", ConstString.KEY);
                return pa;
            }
        };
        mQueue.add(postRequest);
    }

    private void initView() {
        //已经选择
        tag_1 = (TextView) findViewById(R.id.tag_1);
        tag_1.setOnClickListener(this);
        tag_2 = (TextView) findViewById(R.id.tag_2);
        tag_2.setOnClickListener(this);
        tag_3 = (TextView) findViewById(R.id.tag_3);
        tag_3.setOnClickListener(this);
        //自定义标签
        add_layout = (LinearLayout) findViewById(R.id.add_layout);
        add_layout.setOnClickListener(this);
        add_tag = (ImageView) findViewById(R.id.add_tag);
        add_tag.setOnClickListener(this);
        tag_my_1 = (TextView) findViewById(R.id.tag_my_1);
        tag_my_1.setOnClickListener(this);
        tag_my_2 = (TextView) findViewById(R.id.tag_my_2);
        tag_my_2.setOnClickListener(this);
        tag_my_3 = (TextView) findViewById(R.id.tag_my_3);
        tag_my_3.setOnClickListener(this);
        //死标签
        t_1 = (TextView) findViewById(R.id.t_1);
        t_1.setOnClickListener(this);
        t_2 = (TextView) findViewById(R.id.t_2);
        t_2.setOnClickListener(this);
        t_3 = (TextView) findViewById(R.id.t_3);
        t_3.setOnClickListener(this);
        t_4 = (TextView) findViewById(R.id.t_4);
        t_4.setOnClickListener(this);
        t_5 = (TextView) findViewById(R.id.t_5);
        t_5.setOnClickListener(this);
        t_6 = (TextView) findViewById(R.id.t_6);
        t_6.setOnClickListener(this);
        t_7 = (TextView) findViewById(R.id.t_7);
        t_7.setOnClickListener(this);
        t_8 = (TextView) findViewById(R.id.t_8);
        t_8.setOnClickListener(this);
        t_9 = (TextView) findViewById(R.id.t_9);
        t_9.setOnClickListener(this);
    }

    private static String labels = "";
    private void initLabels() {
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if (jsonObject != null) {
            labels = jsonObject.getString("labels");
            if (!TextUtils.isEmpty(labels)) {
                String[] s = labels.split(",");
                if (s != null && s.length > 0) {
                    for (int i = 0; i < s.length; i++) {
                        switch (i) {
                            case 0:
                                tag_1.setVisibility(View.VISIBLE);
                                tag_1.setText(s[i]);
                                break;
                            case 1:
                                tag_2.setVisibility(View.VISIBLE);
                                tag_2.setText(s[i]);
                                break;
                            case 2:
                                tag_3.setVisibility(View.VISIBLE);
                                tag_3.setText(s[i]);
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                if (data != null) {
                    String t = data.getStringExtra("my_tag");
                    if (t != null && t.trim().length() > 0) addTag(t);
                }
                break;
            default:
                break;
        }
    }

}
