package com.socialassistant_youyuelive.activity;

/**
 * Created by zslAdministrator on 2017/4/27.
 */

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
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.UserInfo;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.EditText;

public class ResgitActivity extends AppCompatActivity implements OnClickListener {

    private EditText phoneNumber, pw, confirm_pw, Verification, Verification_code;
    private Button registe;
    private CheckBox check;
    private static final String url = ConstString.IP + "/video/user/register";
    private static final String url_Verification = ConstString.IP + "/video/user/SendCodeMsg";
    private ProgressDialog progressDialog;
    private UserInfo userInfo = null;
    private FireworkView fireworkView1, fireworkView2, fireworkView3, fireworkView4;

    private int time = 60;

    private Context context;

    private TextView jump;
    private static final String url_jump = ConstString.IP + "/video/user/skipRegister";

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
        setContentView(R.layout.activity_regist);
        context = this;
        ActivityCollector.addActivity(this);
        Intent intent = getIntent();
        if (intent != null) userInfo = (UserInfo) intent.getSerializableExtra("userinfo");
        String title = getString(R.string.regist);
        initToolbar(R.id.toolbar, R.id.title, title);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        pw = (EditText) findViewById(R.id.pw);
        pw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (pw.isFocusable()) ChangePWActivity.ShowAlerterAboutPW(ResgitActivity.this);
                return false;
            }
        });
        registe = (Button) findViewById(R.id.regist);
        registe.setOnClickListener(this);
        Verification = (EditText) findViewById(R.id.Verification);
        Verification.setOnClickListener(this);
        Verification.setFocusable(false);
        confirm_pw = (EditText) findViewById(R.id.confirm_pw);
        Verification_code = (EditText) findViewById(R.id.Verification_code);
        check = (CheckBox) findViewById(R.id.check);
        fireworkView1 = (FireworkView) findViewById(R.id.fire_1);
        fireworkView2 = (FireworkView) findViewById(R.id.fire_2);
        fireworkView3 = (FireworkView) findViewById(R.id.fire_3);
        fireworkView4 = (FireworkView) findViewById(R.id.fire_4);
        fireworkView1.bindEditText(phoneNumber);
        fireworkView2.bindEditText(pw);
        fireworkView3.bindEditText(confirm_pw);
        fireworkView4.bindEditText(Verification_code);

        //pw.setFocusable(false);
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        jump = (TextView) findViewById(R.id.jump);
        if (userInfo != null) {
            titleString = getResources().getString(R.string.bangding);
            jump.setVisibility(View.VISIBLE);
        }
        jump.setOnClickListener(this);
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
            case R.id.regist: {
                String phoneNum = phoneNumber.getText().toString().trim();
                //判断手机号
                if (!TextUtils.isEmpty(phoneNum) && judgePhoneNums(phoneNum)) {
                    String pwString1 = pw.getText().toString().trim();
                    String confirmString1 = confirm_pw.getText().toString().trim();
                    if (pwString1 != null && confirmString1 != null && pwString1.equals(confirmString1) && !pwString1.equals("")) {
                        if (!ChangePWActivity.isHaveChinese(pwString1) && !ChangePWActivity.isHaveChinese(confirmString1) && ChangePWActivity.PW(pwString1)) {
                            if (check.isChecked()) {
                                String VerificationCode = Verification_code.getText().toString().trim();
                                if (!TextUtils.isEmpty(VerificationCode)) {
                                    progressDialog = new ProgressDialog(context);
                                    progressDialog.setTitle(getResources().getString(R.string.regist_yue));
                                    progressDialog.setMessage(getResources().getString(R.string.registing));
                                    progressDialog.setCancelable(true);
                                    progressDialog.show();
                                    registered(phoneNum, pwString1, VerificationCode);
                                } else {
                                    ShowToast.normalShow(context, "请填写验证码!", true);
                                }
                            } else {
                                ShowToast.show(context, R.string.agree_po, true);
                            }
                        } else {
                            ChangePWActivity.ShowAlerterAboutPW(ResgitActivity.this);
                            //只能输入字母数字和下划线(6-12位),必须包含字母和数字
                            //ShowToast.show(context, R.string.pw_limit, true);
                        }
                    } else {
                        ShowToast.show(context, R.string.pw_confirm_pw, true);
                    }
                } else {
                    ShowToast.show(context, R.string.err_phone, true);
                }
            }
                break;
            case R.id.jump:
                JUMP_();
                break;
            case R.id.Verification: {
                String phoneNum = phoneNumber.getText().toString().trim();
                //判断手机号
                if (!TextUtils.isEmpty(phoneNum) && judgePhoneNums(phoneNum)) {
                    String pwString1 = pw.getText().toString().trim();
                    String confirmString1 = confirm_pw.getText().toString().trim();
                    if (pwString1 != null && confirmString1 != null && pwString1.equals(confirmString1) && !pwString1.equals("")) {
                        if (!ChangePWActivity.isHaveChinese(pwString1) && !ChangePWActivity.isHaveChinese(confirmString1) && ChangePWActivity.PW(pwString1)) {
                            if (check.isChecked()) {
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
                                ShowToast.show(context, R.string.agree_po, true);
                            }
                        } else {
                            ChangePWActivity.ShowAlerterAboutPW(ResgitActivity.this);
                            //只能输入字母数字和下划线
                            //ShowToast.show(context, R.string.pw_limit, true);
                        }
                    } else {
                        ShowToast.show(context, R.string.pw_confirm_pw, true);
                    }
                } else {
                    ShowToast.show(context, R.string.err_phone, true);
                }
            }
                break;
            default:
                break;
        }
    }

    private void JUMP_() {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url_jump,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            Intent intent = new Intent();
                            intent.setAction(ConstString.MAIN_ACTION);
                            String user = jsonObject.getString("user");
                            if (user != null) intent.putExtra("user", user);
                            intent.putExtra("isLogined", true);
                            ConstString.isLogined = true;
                            ConstString.isLiver = false;
                            intent.putExtra("JPush", true);
                            intent.putExtra("jump", true);
                            String token = jsonObject.getString("token");
                            String key = jsonObject.getString("key");
                            intent.putExtra("token", token);
                            intent.putExtra("key", key);
                            startActivity(intent);
                            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                            if(version  >= 5) {
                                //此为自定义的动画效果，下面两个为系统的动画效果
                                //overridePendingTransition(R.anim.sq_en, R.anim.ex);
                                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                            }
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
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
                Map<String, String> params = new HashMap<>();
                if (userInfo != null) params.put("userinfo", userInfo.toString());
                //手机类型
                params.put("phoneType", "1");
                return params;
            }
        };
        mQueue.add(postRequest);
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
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("limit")) {
                            ShowToast.show(context, R.string.limit_verificationCode, true);
                        } else {
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
                Map<String, String> params = new HashMap<>();
                params.put("mobile", phoneNum);
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    private void registered(final String account, final String pw, final String VerificationCode) {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handler.sendEmptyMessage(2);
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) {
                            ShowToast.show(context, R.string.registered_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ShowToast.show(context, R.string.registered_success, true);
                            if (userInfo != null) {
                                //微信注册
                                Intent intent = new Intent();
                                intent.setAction(ConstString.MAIN_ACTION);
                                String user = jsonObject.getString("user");
                                if (user != null) intent.putExtra("user", user);
                                intent.putExtra("isLogined", true);
                                //如果是主播type = 1;用户 = 0;
                                ConstString.isLiver = (jsonObject.getIntValue("type") == 1);
                                ConstString.isLogined = true;
                                intent.putExtra("JPush", true);
                                String token = jsonObject.getString("token");
                                String key = jsonObject.getString("key");
                                intent.putExtra("token", token);
                                intent.putExtra("key", key);
                                startActivity(intent);
                                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                                if(version  >= 5) {
                                    //此为自定义的动画效果，下面两个为系统的动画效果
                                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                                }
                            } else {
                                //手机注册
                                //进入登录界面
                                startActivity(new Intent(context, PhoneLoginActivity.class));
                                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                                if(version  >= 5) {
                                    //此为自定义的动画效果，下面两个为系统的动画效果
                                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                                }
                            }
                            finish();
                        } else if (state != null && state.equals("occupy")){
                            ShowToast.show(context, R.string.regist_occupy, true);
                        } else if (state != null && state.equals("verificationCode")){
                            ShowToast.show(context, R.string.verificationCode, true);
                        } else {
                            ShowToast.show(context, R.string.registered_fail, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(2);
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
                    ShowToast.show(context, R.string.registered_fail, true);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", pw);
                params.put("verificationCode", VerificationCode);
                if (userInfo != null) {
                    params.put("userinfo", userInfo.toString());
                    params.put("register_type", "0");
                } else {
                    params.put("register_type", "1");
                }
                //手机类型
                params.put("phoneType", "1");
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

