package com.zsl.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;

import com.zsl.Util.ToastUtil;
import com.zsl.bean.NewsBean;
import com.zsl.xiangqin.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SendNewsActivity extends BaseActivity {

    private AppCompatButton send;
    private AppCompatEditText data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendnews);
        data = findViewById(R.id.data);
        send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNews();
            }
        });
    }

    private void sendNews() {
        String s = data.getText().toString();
        if (TextUtils.isEmpty(s)) {
            ToastUtil.normalShow(SendNewsActivity.this, "内容为空!", true);
        } else {
            NewsBean newsBean = new NewsBean();
            newsBean.setNews(s);
            Date d = new Date();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateNowStr = sdf.format(d);
            newsBean.setData(dateNowStr);
            newsBean.save(new SaveListener<String>() {
                @Override
                public void done(String objectId,BmobException e) {
                    if (e == null) {
                        ToastUtil.normalShow(SendNewsActivity.this, "上传成功!", true);
                    } else {
                        ToastUtil.normalShow(SendNewsActivity.this, "上传失败！重新来一次吧！" + e.getMessage(), true);
                    }
                }
            });
        }
    }
}
