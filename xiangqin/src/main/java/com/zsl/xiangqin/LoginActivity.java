package com.zsl.xiangqin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zsl.Util.ActivityCollector;
import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.Util.UrlUtil;
import com.zsl.activity.AboutMeActivity;
import com.zsl.activity.MainActivity;
import com.zsl.activity.RegisterActivity;
import com.zsl.bean.UserBean;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    private AppCompatTextView me;
    private AppCompatTextView zc;

    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            switch (msg.what){
                case 0:{
                    ToastUtil.normalShow(LoginActivity.this, "访问服务器失败!", true);
                    break;
                }
                case 1:{
                    ToastUtil.normalShow(LoginActivity.this, "登录成功!", true);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    break;
                }
                case 2:{
                    ToastUtil.normalShow(LoginActivity.this, "本账号不存在!", true);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        me = findViewById(R.id.me);
        zc = findViewById(R.id.zc);
        me.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, AboutMeActivity.class));
            }
        });
        zc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !validatePassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!judgePhoneNums(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            login(email, password);
        }
    }

    private void login(final String email, final String password) {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setMessage(getResources().getString(R.string.loging));
        progressDialog.setCancelable(true);
        progressDialog.show();

        OkHttpClient mHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", email)
                .add("password", password)
                .add("type", "0")
                .build();

        Request request = new Request.Builder()
                .url(UrlUtil.loginUrl)
                .post(formBody)
                .build();

        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Logger.d("TAG", str);
                JSONObject jsonObject = JSON.parseObject(str);
                int status = jsonObject.getIntValue("status");
                switch (status) {
                    case 0:
                        handler.sendEmptyMessage(1);
                        UserBean userBean = new UserBean();
                        userBean.setPhoneNumber(email);
                        userBean.setPassword(password);
                        userBean.save();
                        break;
                    case 1:handler.sendEmptyMessage(2);break;
                }
            }

        });

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

    public static Boolean validatePassword(String passWord) {
        return  !isHaveChinese(passWord) && PW(passWord);
    }

    public static boolean isHaveChinese(String str) {
        String digits = "_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ";
        for(int i = 0; i < str.length(); i++){
            if(!(digits.indexOf(str.charAt(i)) >= 0)){
                return true;
            }
        }
        return false;
    }

    public static boolean PW(String s) {
        //String str1 ="[0-9a-zA-Z|_]{6,12}";
        //return str1.matches(s);
        //密码6-12位
        if (s.length() >= 6 && s.length() <= 12) {
            //密码只能字母数字，下划线
            String digits = "_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ";
            boolean flag = true;
            for(int i = 0; i < s.length(); i++){
                if(!(digits.indexOf(s.charAt(i)) >= 0)){
                    flag = false;
                }
            }
            if (flag) {
                //密码中必须包含字母和数字
                String di = "0123456789";
                boolean haveNumber = false;
                for(int i = 0; i < s.length(); i++){
                    if(di.indexOf(s.charAt(i)) >= 0){
                        haveNumber = true;
                    }
                }
                if (haveNumber) {
                    String d = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ";
                    for(int i = 0; i < s.length(); i++){
                        if(d.indexOf(s.charAt(i)) >= 0){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        ActivityCollector.finishAll();
        System.exit(0);
    }
}

