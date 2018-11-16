package com.socialassistant_youyuelive.activity;

import java.util.HashMap;
import java.util.Map;

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
import com.socialassistant_youyuelive.AV.DemoApplication;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.fireEditText.FireworkView;
import com.socialassistant_youyuelive.util.ShowToast;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ForgetPWActivity extends AppCompatActivity implements OnClickListener {

    private EditText phoneNumber, Verification, Verification_code;
    private Button registe;
    private static final String url = ConstString.IP + "/video/user/resetPassword";
    private static final String url_Verification = ConstString.IP + "/video/user/SendCodeMsg";
    private ProgressDialog progressDialog;
    private FireworkView fireworkView1, fireworkView2;

    private int time = 60;

    private Context context;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Verification.setText(getResources().getString(R.string.send_again) + "(" + time + ")");
            } else if (msg.what == 1) {
                Verification.setText(getResources().getString(R.string.get_Verification_code));
                Verification.setEnabled(true);
                time = 60;
            } else if (msg.what == 2) {
                progressDialog.dismiss();
            }
        }

    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pw);
        initToolbar(R.id.toolbar, R.id.title, getString(R.string.forget_pw));
        context = this;
        ActivityCollector.addActivity(this);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        registe = (Button) findViewById(R.id.regist);
        registe.setOnClickListener(this);
        Verification = (EditText) findViewById(R.id.Verification);
        Verification.setOnClickListener(this);
        Verification.setFocusable(false);
        Verification_code = (EditText) findViewById(R.id.Verification_code);
        fireworkView1 = (FireworkView) findViewById(R.id.fire_1);
        fireworkView2 = (FireworkView) findViewById(R.id.fire_2);
        fireworkView1.bindEditText(phoneNumber);
        fireworkView2.bindEditText(Verification_code);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(this, PhoneLoginActivity.class));
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(version  >= 5) {
                //此为自定义的动画效果，下面两个为系统的动画效果
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.regist:
                String phone = phoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phone) && judgePhoneNums(phone)) {
                    String VerificationCode = Verification_code.getText().toString().trim();
                    if (!TextUtils.isEmpty(VerificationCode)) {
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(getResources().getString(R.string.get_pw));
                        progressDialog.setMessage(getResources().getString(R.string.getting_pw));
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                        registered_forget(phone, VerificationCode);
                    } else {
                        ShowToast.show(context, R.string.Information_complete, true);
                    }
                } else {
                    ShowToast.show(context, R.string.err_phone, true);
                }
                break;
            case R.id.Verification:
                String phoneNum = phoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneNum) && judgePhoneNums(phoneNum)) {
                    Verification.setText(getResources().getString(R.string.send_again) + "(" + time + ")");
                    Verification.setEnabled(false);
                    GetVerificationCode(phoneNum);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (; time > 0; time--) {
                                handler.sendEmptyMessage(0);
                                if (time <= 0) {
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(1);
                        }
                    }).start();
                } else {
                    ShowToast.show(context, R.string.input_phoneNumber, true);
                }
                break;
            default:
                break;
        }
    }

    private void GetVerificationCode(final String phoneNum) {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url_Verification,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) {
                            ShowToast.show(context, R.string.access_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("limit")) {
                            ShowToast.show(context, R.string.limit_verificationCode, true);
                        } else if (state != null && state.equals("ok")) {
                            ShowToast.show(context, R.string.later, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (error instanceof NetworkError) {
                    ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                } else if (error instanceof ServerError) {
                    ShowToast.normalShow(context, "服务器异常，请稍后重试!", true);
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
                Map<String, String> params = new HashMap<>();
                params.put("mobile", phoneNum);
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    private void registered_forget(final String phone, final String VerificationCode) {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) {
                            ShowToast.show(context, R.string.access_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ShowToast.show(context, R.string.new_pw, true);
                            //进入登录界面
                            startActivity(new Intent(ForgetPWActivity.this, PhoneLoginActivity.class));
                            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                            if(version  >= 5) {
                                //此为自定义的动画效果，下面两个为系统的动画效果
                                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                            }
                            finish();
                        } else if (state != null && state.equals("verificationCode")){
                            ShowToast.show(context, R.string.verificationCode, true);
                        } else {
                            ShowToast.show(context, R.string.get_pw_fail, true);
                        }
                        handler.sendEmptyMessage(2);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (error instanceof NetworkError) {
                    ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                } else if (error instanceof ServerError) {
                    ShowToast.normalShow(context, "服务器异常，请稍后重试!", true);
                } else if (error instanceof AuthFailureError) {
                    ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                } else if (error instanceof NoConnectionError) {
                    ShowToast.normalShow(context, "本地网络链接异常!", true);
                } else if (error instanceof TimeoutError) {
                    ShowToast.normalShow(context, "访问服务器超时!", true);
                } else {
                    ShowToast.show(context, R.string.get_pw_fail, true);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", phone);
                params.put("verificationCode", VerificationCode);
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11) && isMobileNO(phoneNums)) {
            return true;
        }
        ShowToast.show(context, R.string.err_phone, true);
        return false;
    }

    /**
     * 判断一个字符串的位数
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length;
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobileNums) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            startActivity(new Intent(this, PhoneLoginActivity.class));
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(version  >= 5) {
                //此为自定义的动画效果，下面两个为系统的动画效果
                overridePendingTransition(R.anim.en, R.anim.ex);
            }*/
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}

