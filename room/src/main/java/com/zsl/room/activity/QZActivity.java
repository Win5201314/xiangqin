package com.zsl.room.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zsl.room.R;
import com.zsl.room.bean.QZBean;
import com.zsl.room.util.ToastUtil;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class QZActivity extends BaseActivity implements View.OnClickListener {

    private EditText sq, address, money, glc, xq, cw;
    private Button send;
    private QZBean qzBean = new QZBean();
    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    progressDialog.dismiss();
                    finish();
                    break;
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qz);
        initView();
        send.setOnClickListener(this);
    }

    private void initView() {
        sq = findViewById(R.id.sq);
        address = findViewById(R.id.address);
        money = findViewById(R.id.money);
        glc = findViewById(R.id.glc);
        xq = findViewById(R.id.xq);
        cw = findViewById(R.id.cw);
        send = findViewById(R.id.send);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send) {
            sq.setError(null);
            address.setError(null);
            money.setError(null);
            glc.setError(null);
            xq.setError(null);
            cw.setError(null);
            boolean cancel = false;
            View focusView = null;
            String kong = "不能为空!";
            String a = cw.getText().toString();
            if (TextUtils.isEmpty(a)) {
                cw.setError(kong);
                focusView = cw;
                cancel = true;
            } else {
                qzBean.setCw(a);
            }
            String b = xq.getText().toString();
            if (TextUtils.isEmpty(b)) {
                xq.setError(kong);
                focusView = xq;
                cancel = true;
            } else {
                qzBean.setXq(b);
            }

            String c = glc.getText().toString();
            if (TextUtils.isEmpty(c)) {
                glc.setError(kong);
                focusView = glc;
                cancel = true;
            } else {
                qzBean.setGao(c);
            }

            String d = money.getText().toString();
            if (TextUtils.isEmpty(d)) {
                money.setError(kong);
                focusView = money;
                cancel = true;
            } else {
                qzBean.setMoney(d);
            }
            String e = address.getText().toString();
            if (TextUtils.isEmpty(e)) {
                address.setError(kong);
                focusView = address;
                cancel = true;
            } else {
                qzBean.setYq(e);
            }
            String f = sq.getText().toString();
            if (TextUtils.isEmpty(f)) {
                sq.setError("不能为空");
                focusView = sq;
                cancel = true;
            } else {
                qzBean.setSq(f);
            }
            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                //上传
                upLoad(qzBean);
            }
        }
    }

    private void upLoad(QZBean qzBean) {
        progressDialog = new ProgressDialog(QZActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setMessage("正在上传...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        qzBean.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                handler.sendEmptyMessage(0);
                if (e == null) {
                    ToastUtil.normalShow(QZActivity.this, "上传成功!", true);
                } else {
                    ToastUtil.normalShow(QZActivity.this, "上传失败！重新来一次吧！" + e.getMessage(), true);
                }
            }
        });
    }
}
