package com.zsl.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zsl.Util.ActivityCollector;
import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.Util.UrlUtil;
import com.zsl.bean.UserBean;
import com.zsl.xiangqin.LoginActivity;
import com.zsl.xiangqin.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText comfirmPasswordView;

    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            switch (msg.what){
                case 0:{
                    ToastUtil.normalShow(RegisterActivity.this, "访问服务器失败!", true);
                    break;
                }
                case 1:{
                    ToastUtil.normalShow(RegisterActivity.this, "注册成功!", true);
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                    break;
                }
                case 2:{
                    ToastUtil.normalShow(RegisterActivity.this, "本账号已经注册过了!", true);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActivityCollector.addActivity(this);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        comfirmPasswordView = findViewById(R.id.comfirm_password);

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
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
        String comfirmPW = comfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(comfirmPW) || !LoginActivity.validatePassword(comfirmPW)) {
            comfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = comfirmPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !LoginActivity.validatePassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!LoginActivity.judgePhoneNums(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (!password.equals(comfirmPW)) {
            comfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = comfirmPasswordView;
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
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(true);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient mHttpClient = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("username", email)
                        .add("password", password)
                        .add("type", "1")
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
                            case 2:
                                handler.sendEmptyMessage(1);
                                UserBean userBean = new UserBean();
                                userBean.setPhoneNumber(email);
                                userBean.setPassword(password);
                                userBean.save();
                                break;
                            case 3:handler.sendEmptyMessage(2);break;
                        }
                    }

                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
