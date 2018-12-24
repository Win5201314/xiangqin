package com.zsl.room.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.squareup.picasso.Picasso;
import com.yanzhenjie.album.Album;
import com.zsl.room.R;
import com.zsl.room.bean.HomeBean;
import com.zsl.room.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

public class SendActivity extends BaseActivity implements View.OnClickListener{

    private EditText sq, address, xq, dt, money, qk;
    private RadioGroup type;
    private Button image, send;
    private HomeBean homeBean = new HomeBean();

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
                case 1: {
                    upload(homeBean);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        initView();
        image.setOnClickListener(this);
        send.setOnClickListener(this);
        homeBean.setType("合租");
        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.hz:
                        homeBean.setType("合租");
                        break;
                    case R.id.zz:
                        homeBean.setType("转租");
                        break;
                }
            }
        });
    }

    private void initView() {
        sq = findViewById(R.id.sq);
        address = findViewById(R.id.address);
        xq = findViewById(R.id.xq);
        dt = findViewById(R.id.dt);
        money = findViewById(R.id.money);
        qk = findViewById(R.id.qk);
        type = findViewById(R.id.type);
        image = findViewById(R.id.image);
        send = findViewById(R.id.send);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send) {
            homeBean.setSq(sq.getText().toString());
            homeBean.setAddress(address.getText().toString());
            homeBean.setIsDianTi(dt.getText().toString());
            homeBean.setMoney(money.getText().toString());
            homeBean.setIsXiaoQu(xq.getText().toString());
            homeBean.setQk(qk.getText().toString());

            sq.setError(null);
            address.setError(null);
            money.setError(null);
            dt.setError(null);
            xq.setError(null);
            qk.setError(null);
            boolean cancel = false;
            View focusView = null;
            String kong = "不能为空!";
            String a = qk.getText().toString();
            if (TextUtils.isEmpty(a)) {
                qk.setError(kong);
                focusView = qk;
                cancel = true;
            } else {
                homeBean.setQk(a);
            }
            String b = xq.getText().toString();
            if (TextUtils.isEmpty(b)) {
                xq.setError(kong);
                focusView = xq;
                cancel = true;
            } else {
                homeBean.setIsXiaoQu(b);
            }

            String c = dt.getText().toString();
            if (TextUtils.isEmpty(c)) {
                dt.setError(kong);
                focusView = dt;
                cancel = true;
            } else {
                homeBean.setIsDianTi(c);
            }

            String d = money.getText().toString();
            if (TextUtils.isEmpty(d)) {
                money.setError(kong);
                focusView = money;
                cancel = true;
            } else {
                homeBean.setMoney(d);
            }
            String e = address.getText().toString();
            if (TextUtils.isEmpty(e)) {
                address.setError(kong);
                focusView = address;
                cancel = true;
            } else {
                homeBean.setAddress(e);
            }
            String f = sq.getText().toString();
            if (TextUtils.isEmpty(f)) {
                sq.setError("不能为空");
                focusView = sq;
                cancel = true;
            } else {
                homeBean.setSq(f);
            }
            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                //上传
                String s = path;
                if (!TextUtils.isEmpty(s)) {
                    String[] filePaths = s.split(",");
                    sendBmo(filePaths);
                } else {
                    upload(homeBean);
                }
            }
        }
        if (view.getId() == R.id.image) {
            ToastUtil.normalShow(this, "最多选择4张", true);
            getAlbum("房源图片", 4, 666);
        }
    }

    //上传投稿
    private void sendBmo(final String[] filePaths) {
        BmobFile.uploadBatch(filePaths, new UploadBatchListener() {

            @Override
            public void onSuccess(List<BmobFile> files, List<String> urls) {
                //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                //2、urls-上传文件的完整url地址
                for (String s : urls) {
                    Log.d("TAG", "==============" + s);
                }
                if(urls.size() == filePaths.length){//如果数量相等，则代表文件全部上传完成
                    //do something
                    StringBuffer m = new StringBuffer();
                    for (String s : urls) { m.append(s + ","); }
                    String ss = m.substring(0, m.length() - 1);
                    homeBean.setImage(ss);
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                ToastUtil.normalShow(SendActivity.this, "错误码"+statuscode +",错误描述："+errormsg, true);
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）
            }
        });
    }

    private void upload(HomeBean homeBean) {
        progressDialog = new ProgressDialog(SendActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setMessage("正在上传...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        homeBean.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                handler.sendEmptyMessage(0);
                if (e == null) {
                    ToastUtil.normalShow(SendActivity.this, "上传成功!", true);
                } else {
                    ToastUtil.normalShow(SendActivity.this, "上传失败！重新来一次吧！" + e.getMessage(), true);
                }
            }
        });
    }

    private ArrayList<String> mImageList = new ArrayList<>();
    private void getAlbum(String title, int number, int code) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(SendActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SendActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Album.album(SendActivity.this)
                    .toolBarColor(getResources().getColor(R.color.colorAccent)) // Toolbar color.
                    .statusBarColor(getResources().getColor(R.color.colorAccent)) // StatusBar color.
                    .navigationBarColor(getResources().getColor(R.color.colorAccent)) // NavigationBar color.
                    .title(title) // Title.
                    .selectCount(number) // Choose up to a few pictures.
                    .columnCount(2) // Number of albums.
                    .camera(true) // Have a camera function.
                    .checkedList(mImageList) // Has selected the picture, automatically select.
                    .start(code); // 999 is requestCode.
        }
    }

    String path = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 666) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                ToastUtil.normalShow(SendActivity.this, "选择完毕!", true);
                mImageList = Album.parseResult(data);
                if (mImageList != null && mImageList.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (String s : mImageList) { sb.append(s + ","); }
                    String m = sb.toString();
                    m = m.substring(0, m.length() - 1);
                    path = m;
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
