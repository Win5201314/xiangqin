package com.zsl.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.Util.UrlUtil;
import com.zsl.xiangqin.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JoinGroupActivity extends BaseActivity {

    private AppCompatTextView show;
    private ProgressDialog progressDialog;
    private String text = "";

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Context context = JoinGroupActivity.this;
            progressDialog.dismiss();
            switch (msg.what){
                case 0:{
                    ToastUtil.normalShow(context, "访问服务器失败!", true);
                    break;
                }
                case 1:{
                    show.setText(text);
                    break;
                }
                case 2: {
                    ToastUtil.normalShow(context, "获取数据失败!", true);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        show = findViewById(R.id.show);
        //获取群名
        //joinGroup();
    }

    private void joinGroup() {
        progressDialog = new ProgressDialog(JoinGroupActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setMessage("正在获取群名...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        OkHttpClient mHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .url(UrlUtil.GroupUrl)
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
                StringBuilder stringBuilder = new StringBuilder();
                JSONArray jsonArray = jsonObject.getJSONArray("group");
                if (jsonArray != null && jsonArray.size() >= 1) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jb = jsonArray.getJSONObject(i);
                        if (jb == null) continue;
                        String name = jb.getString("groupName");
                        String number = jb.getString("groupId");
                        stringBuilder.append("群名：" + name + "群号码：" + number + "\n");
                    }
                    text = stringBuilder.toString();
                    handler.sendEmptyMessage(1);
                } else {
                    handler.sendEmptyMessage(2);
                }
            }

        });
    }
}
