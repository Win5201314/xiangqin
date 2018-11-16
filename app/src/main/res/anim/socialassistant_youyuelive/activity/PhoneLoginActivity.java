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
import com.socialassistant_youyuelive.AV.md5.MD5;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.UserInfo;
import com.socialassistant_youyuelive.fireEditText.FireworkView;
import com.socialassistant_youyuelive.util.ShowToast;
import com.socialassistant_youyuelive.wxapi.WXEntryActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by zslAdministrator on 2017/4/27.
 */

public class PhoneLoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText phoneNumber, pw;
    private Button login;
    private TextView resgit, forget_pw;
    private FireworkView mFireworkView, fireworkView;
    private AppCompatCheckBox keep;
    private ProgressDialog progressDialog;
    private UserInfo userInfo = null;
    private Context context;
    private static final String url = ConstString.IP + "/video/user/userLogin";

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) progressDialog.dismiss();
        }

    };
    //private Animation myAnimation_Scale;
    //ParticleView particleAnimator;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initToolbar(R.id.toolbar, R.id.title, "登录");
        ActivityCollector.addActivity(this);
        context = this;
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        pw = (EditText) findViewById(R.id.pw);
        keep = (AppCompatCheckBox) findViewById(R.id.keep);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        resgit = (TextView) findViewById(R.id.resgit);
        resgit.setOnClickListener(this);
        forget_pw = (TextView) findViewById(R.id.forget_pw);
        forget_pw.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            userInfo = (UserInfo) intent.getSerializableExtra("userinfo");
            if (userInfo != null) resgit.setVisibility(View.INVISIBLE);
        }
        mFireworkView = (FireworkView) findViewById(R.id.fire);
        mFireworkView.bindEditText(phoneNumber);
        fireworkView = (FireworkView) findViewById(R.id.fire_1);
        fireworkView.bindEditText(pw);
        /*myAnimation_Scale =new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        myAnimation_Scale.setDuration(2000);
        login.setAnimation(myAnimation_Scale);*/
        /*
        particleAnimator = new ParticleView(this, 3000);
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
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                particleAnimator.boom(v);//getWindow().getDecorView().findViewById(android.R.id.content)
            }
        });*/
        SharedPreferences sp = getSharedPreferences("ACCOUT", Context.MODE_PRIVATE);
        phoneNumber.setText(sp.getString("accout", ""));
        pw.setText(sp.getString("pw", ""));
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
            startActivity(new Intent(this, WXEntryActivity.class));
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int version1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(version1  >= 5) {
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
            case R.id.login:
                String phone = phoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phone) && judgePhoneNums(phone)) {
                    String password = pw.getText().toString().trim();
                    if (!TextUtils.isEmpty(password) && !ChangePWActivity.isHaveChinese(password)
                            && ChangePWActivity.PW(password)) {
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(getResources().getString(R.string.login_yue));
                        progressDialog.setMessage(getResources().getString(R.string.loging));
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                        if (userInfo != null) {
                            Login(phone, password, true);
                        } else {
                            Login(phone, password, false);
                        }
                    } else {
                        //只能输入字母数字和下划线(6-12位),必须包含字母和数字
                        ShowToast.show(context, R.string.pw_limit, true);
                    }
                } else {
                    ShowToast.show(context, R.string.err_phone, true);
                }
                break;
            case R.id.resgit:
                startActivity(new Intent(this, ResgitActivity.class));
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                if(version  >= 5) {
                    //此为自定义的动画效果，下面两个为系统的动画效果
                    overridePendingTransition(R.anim.en, R.anim.ex);
                }*/
                finish();
                break;
            case R.id.forget_pw:
                startActivity(new Intent(this, ForgetPWActivity.class));
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
                if(v1 >= 5) {
                    //此为自定义的动画效果，下面两个为系统的动画效果
                    overridePendingTransition(R.anim.en, R.anim.ex);
                }*/
                finish();
                break;
            default:
                break;
        }
    }

    private void Login(final String account, final String pw, final boolean isWX) {
        // TODO Auto-generated method stub
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
                            ShowToast.show(context, R.string.login_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            //记住密码
                            if (keep.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("ACCOUT", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("accout", account);
                                editor.putString("pw", pw);
                                editor.commit();
                            }
                            ShowToast.show(context, R.string.login_success, true);
                            //进入主界面
                            Intent intent = new Intent();
                            intent.setAction(ConstString.MAIN_ACTION);
                            String user = jsonObject.getString("user");
                            if (user != null) intent.putExtra("user", user);
                            //如果是主播type = 1;用户 = 0;
                            ConstString.isLiver = (jsonObject.getIntValue("type") == 1);
                            intent.putExtra("isLogined", true);
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
                            finish();
                        } else {
                            ShowToast.show(context, R.string.login_fail, true);
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
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", MD5.getStringMD5(pw));
                if (isWX) {
                    params.put("userinfo", userInfo.toString());
                    params.put("login_type", "0");
                } else {
                    params.put("login_type", "1");
                }
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
    public static boolean judgePhoneNums(String phoneNums) {
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
            startActivity(new Intent(context, WXEntryActivity.class));
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(version  >= 5) {
                //此为自定义的动画效果，下面两个为系统的动画效果
                overridePendingTransition(R.anim.dialog_enter, -1);
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

