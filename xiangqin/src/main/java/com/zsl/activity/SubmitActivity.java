package com.zsl.activity;

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
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.cf.androidpickerlibrary.AddressPicker;
import com.cf.androidpickerlibrary.DatePicker;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.squareup.picasso.Picasso;
import com.yanzhenjie.album.Album;
import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.bean.BeforeDetail;
import com.zsl.oss.OSSData;
import com.zsl.oss.OssService;
import com.zsl.xiangqin.LoginActivity;
import com.zsl.xiangqin.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import cn.qqtheme.framework.picker.OptionPicker;

public class SubmitActivity extends BaseActivity implements View.OnClickListener {

    //名字
    private EditText name;
    //性别
    private RadioGroup sex;
    //家乡
    private EditText jx;
    //出生年月
    private EditText birthday;
    //现居地
    private EditText xjd;
    //身高
    private EditText sg;
    //体重
    private EditText tz;
    //星座
    private EditText xz;
    //学历
    private EditText xl;
    //职业工作
    private EditText job;
    //平均月薪
    private EditText yx;
    //车房情况
    private EditText carAndHome;
    //个人性格描述
    private EditText xg;
    //个人规划和打算
    private EditText ds;
    //对另一半的要求
    private EditText yq;
    //家里还有哪些成员
    private EditText cy;
    //有无婚史
    private RadioGroup hs;
    //手机号(微信同号)
    private EditText phone;
    //邮箱地址
    private EditText email;
    //额外补充
    private EditText bc;
    //三张图片
    private AppCompatImageView image1, image2, image3;
    //投稿
    private Button send;

    private NumberProgressBar update_progress;

    BeforeDetail detail = new BeforeDetail();

    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    ToastUtil.normalShow(SubmitActivity.this, "上传成功!审核通过之后我们会展示到主页!", true);
                    finish();
                    break;
                }
                case 1: {
                    //上传个人数据[图片上传完之后的]
                    sendDetail();
                    break;
                }
                case 2: {
                    progressDialog.dismiss();
                    break;
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        initView();
        detail.setSex("男");
        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.man:
                        detail.setSex("男");
                        break;
                    case R.id.woman:
                        detail.setSex("女");
                        break;
                }
            }
        });
        detail.setMarry("无");
        hs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.you:
                        detail.setMarry("有");
                        break;
                    case R.id.wu:
                        detail.setMarry("无");
                        break;
                }
            }
        });
    }

    private void initView() {
        name = findViewById(R.id.name);
        sex = findViewById(R.id.sex);
        jx = findViewById(R.id.jx);jx.setOnClickListener(this);
        birthday = findViewById(R.id.birthday);birthday.setOnClickListener(this);
        xjd = findViewById(R.id.xjd);xjd.setOnClickListener(this);
        sg = findViewById(R.id.sg);
        tz = findViewById(R.id.tz);
        xz = findViewById(R.id.xz);
        xl = findViewById(R.id.xl);xl.setOnClickListener(this);
        job = findViewById(R.id.job);
        yx = findViewById(R.id.yx);
        carAndHome = findViewById(R.id.carAndHome);
        xg = findViewById(R.id.xg);
        ds = findViewById(R.id.plan);
        yq = findViewById(R.id.yq);
        cy = findViewById(R.id.cy);
        hs = findViewById(R.id.hs);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        image1 = findViewById(R.id.image1);
        image1.setOnClickListener(this);
        image2 = findViewById(R.id.image2);
        image2.setOnClickListener(this);
        image3 = findViewById(R.id.image3);
        image3.setOnClickListener(this);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        update_progress = findViewById(R.id.update_progress);
        bc = findViewById(R.id.bc);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.jx: {
                AddressPicker picker = new AddressPicker(SubmitActivity.this);

                picker.setAddressListener(new AddressPicker.OnAddressListener() {
                    @Override
                    public void onAddressSelected(String province, String city, String area) {
                        jx.setText(String.valueOf(province + "-" + city + "-" + area));
                    }
                });

                picker.show();
                break;
            }
            case R.id.xjd: {
                AddressPicker picker = new AddressPicker(SubmitActivity.this);

                picker.setAddressListener(new AddressPicker.OnAddressListener() {
                    @Override
                    public void onAddressSelected(String province, String city, String area) {
                        xjd.setText(String.valueOf(province + "-" + city + "-" + area));
                    }
                });

                picker.show();
                break;
            }
            case R.id.birthday: {
                DatePicker picker = new DatePicker(SubmitActivity.this);

                picker.setDateListener(new DatePicker.OnDateCListener() {
                    @Override
                    public void onDateSelected(String year, String month, String day) {
                        birthday.setText(String.valueOf(year + "-" + month + "-" + day));
                    }
                });

                picker.show();
                break;
            }
            case R.id.xl: {
                OptionPicker picker = new OptionPicker(this, new String[]{
                        "大学本科", "专科", "研究生", "硕士", "博士", "小学", "初中", "高中", "其它"
                });
                picker.setOffset(2);
                picker.setSelectedIndex(0);
                picker.setTextSize(11);
                picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        xl.setText(option);
                    }
                });

                picker.show();
                break;
            }
            case R.id.send: {
                sendGao();
                break;
            }
            case R.id.image1: {
                getAlbum("选择三张你的图片", 3, 666);
                break;
            }
            case R.id.image2: {
                getAlbum("选择三张你的图片", 3, 666);
                break;
            }
            case R.id.image3: {
                getAlbum("选择三张你的图片", 3, 666);
                break;
            }
        }
    }

    private void sendGao() {
        name.setError(null);
        jx.setError(null);
        birthday.setError(null);
        xjd.setError(null);
        sg.setError(null);
        tz.setError(null);
        xz.setError(null);
        xl.setError(null);
        job.setError(null);
        yx.setError(null);
        carAndHome.setError(null);
        xg.setError(null);
        ds.setError(null);
        yq.setError(null);
        cy.setError(null);
        phone.setError(null);
        email.setError(null);
        bc.setError(null);
        //
        boolean cancel = false;
        View focusView = null;
        //
        /*String E = email.getText().toString();
        if (TextUtils.isEmpty(E)) {
            email.setError(getString(R.string.noNull));
            focusView = email;
            cancel = true;
        } else {
            detail.setEmail(E);
        }*/
        String ph = phone.getText().toString();
        if (TextUtils.isEmpty(ph)) {
            phone.setError(getString(R.string.noNull));
            focusView = phone;
            cancel = true;
        } else if (!LoginActivity.judgePhoneNums(ph)) {
            phone.setError(getString(R.string.error_invalid_email));
            focusView = phone;
            cancel = true;
        } else {
            detail.setPhone(ph);
        }
        String YQ = yq.getText().toString();
        if (TextUtils.isEmpty(YQ)) {
            yq.setError(getString(R.string.noNull));
            focusView = yq;
            cancel = true;
        } else {
            detail.setRequirement(YQ);
        }
        /*String DS = ds.getText().toString();
        if (TextUtils.isEmpty(DS)) {
            ds.setError(getString(R.string.noNull));
            focusView = ds;
            cancel = true;
        } else {
            detail.setPlanning(DS);
        }*/
        /*String XG = xg.getText().toString();
        if (TextUtils.isEmpty(XG)) {
            xg.setError(getString(R.string.noNull));
            focusView = xg;
            cancel = true;
        } else {
            detail.setCharacter(XG);
        }*/
        String car = carAndHome.getText().toString();
        if (TextUtils.isEmpty(car)) {
            carAndHome.setError(getString(R.string.noNull));
            focusView = carAndHome;
            cancel = true;
        } else {
            detail.setCar(car);
        }
        String YX = yx.getText().toString();
        if (TextUtils.isEmpty(YX)) {
            yx.setError(getString(R.string.noNull));
            focusView = yx;
            cancel = true;
        } else {
            detail.setSalary(YX);
        }
        String jb = job.getText().toString();
        if (TextUtils.isEmpty(jb)) {
            job.setError(getString(R.string.noNull));
            focusView = job;
            cancel = true;
        } else {
            detail.setOccupation(jb);
        }
        String XL = xl.getText().toString();
        if (TextUtils.isEmpty(XL)) {
            xl.setError(getString(R.string.noNull));
            focusView = xl;
            cancel = true;
        } else {
            detail.setEducation(XL);
        }
        String w = tz.getText().toString();
        if (TextUtils.isEmpty(w)) {
            tz.setError(getString(R.string.noNull));
            focusView = tz;
            cancel = true;
        } else {
            detail.setWeight(w);
        }
        String Sg = sg.getText().toString();
        if (TextUtils.isEmpty(Sg)) {
            sg.setError(getString(R.string.noNull));
            focusView = sg;
            cancel = true;
        } else {
            detail.setHeight(Sg);
        }
        String xj = xjd.getText().toString();
        if (TextUtils.isEmpty(xj)) {
            xjd.setError(getString(R.string.noNull));
            focusView = xjd;
            cancel = true;
        } else {
            detail.setResidence(xj);
        }
        String b = birthday.getText().toString();
        if (TextUtils.isEmpty(b)) {
            birthday.setError(getString(R.string.noNull));
            focusView = birthday;
            cancel = true;
        } else {
            detail.setBirthday(b);
        }
        String j = jx.getText().toString();
        if (TextUtils.isEmpty(j)) {
            jx.setError(getString(R.string.noNull));
            focusView = jx;
            cancel = true;
        } else {
            detail.setPath(j);
        }
        String n = name.getText().toString();
        if (TextUtils.isEmpty(n)) {
            name.setError(getString(R.string.noNull));
            focusView = name;
            cancel = true;
        } else {
            detail.setName(n);
        }
        String bcbc = bc.getText().toString();
        if (TextUtils.isEmpty(bcbc)) detail.setBc("");
        else detail.setBc(bcbc);
        //
        /*String XZ = xz.getText().toString();
        if (!TextUtils.isEmpty(XZ)) {
           detail.setConstellation(XZ);
        }
        String F = cy.getText().toString();
        if (!TextUtils.isEmpty(F)) {
            detail.setFamily(F);
        }*/
        //
        if (mImageList != null && mImageList.size() == 3) {
            String sr = mImageList.get(0) + ";" + mImageList.get(1) + ";" + mImageList.get(2);
            detail.setImageUrl(sr);
        } else {
            ToastUtil.normalShow(SubmitActivity.this, "图片没有三张!", true);
            return;
        }
        //
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            ToastUtil.normalShow(SubmitActivity.this, "正在上传,莫慌!", true);
            //SEND("", "");
            String[] filePaths = new String[3];
            filePaths[0] = mImageList.get(0);
            filePaths[1] = mImageList.get(1);
            filePaths[2] = mImageList.get(2);
            progressDialog = new ProgressDialog(SubmitActivity.this);
            progressDialog.setTitle(getResources().getString(R.string.app_name));
            progressDialog.setMessage("正在上传...");
            progressDialog.setCancelable(true);
            progressDialog.show();
            sendBmo(filePaths);
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
                    String s = urls.get(0) + ";" + urls.get(1) + ";" + urls.get(2);
                    detail.setImageUrl(s);
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                ToastUtil.normalShow(SubmitActivity.this, "错误码"+statuscode +",错误描述："+errormsg, true);
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

    //上传投稿
    private void SEND(String filename, String filePath) {
        update_progress.setVisibility(View.VISIBLE);
        //初始化OssService类，参数分别是Content，accessKeyId，accessKeySecret，endpoint，bucketName（后4个参数是您自己阿里云Oss中参数）
        OssService ossService = new OssService(getApplicationContext(), OSSData.accessKeyId, OSSData.accessKeySecret, OSSData.endpoint, OSSData.bucketName);
        //初始化OSSClient
        ossService.initOSSClient();
        //开始上传，参数分别为content，上传的文件名filename，上传的文件路径filePath
        ossService.beginupload(SubmitActivity.this, filename, filePath);
        //上传的进度回调

        ossService.setProgressCallback(new OssService.ProgressCallback() {
            @Override
            public void onProgressCallback(final double progress) {
                Logger.d("TAG", "上传进度：" + progress);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        update_progress.setProgress((int) progress);
                        if ((int) progress == 100) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                });
            }
        });

    }

    private ArrayList<String> mImageList = new ArrayList<>();
    private void getAlbum(String title, int number, int code) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(SubmitActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /*if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //已经禁止提示了
                ShowToast.show(context, R.string.NO_PERMISSION, true);
            } else {
                ActivityCompat.requestPermissions(BecomeAnchorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }*/
            ActivityCompat.requestPermissions(SubmitActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Album.album(SubmitActivity.this)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 666) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                mImageList = Album.parseResult(data);
                if (mImageList != null && mImageList.size() == 3) {
                    String path1 = mImageList.get(0);
                    if (!TextUtils.isEmpty(path1)) {
                        File file = new File(path1);
                        //加载本地文件
                        Picasso.with(SubmitActivity.this).load(file).into(image1);
                    }
                    String path2 = mImageList.get(1);
                    if (!TextUtils.isEmpty(path2)) {
                        File file = new File(path2);
                        //加载本地文件
                        Picasso.with(SubmitActivity.this).load(file).into(image2);
                    }
                    String path3 = mImageList.get(2);
                    if (!TextUtils.isEmpty(path3)) {
                        File file = new File(path3);
                        //加载本地文件
                        Picasso.with(SubmitActivity.this).load(file).into(image3);
                    }
                } else {
                    ToastUtil.normalShow(SubmitActivity.this, "必须同时选择三张!", true);
                    image1.setImageResource(R.mipmap.dsg);
                    image2.setImageResource(R.mipmap.dsg);
                    image3.setImageResource(R.mipmap.dsg);
                    return;
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDetail() {
        detail.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                handler.sendEmptyMessage(2);
                if (e == null) {
                    ToastUtil.normalShow(SubmitActivity.this, "上传成功!", true);
                } else {
                    ToastUtil.normalShow(SubmitActivity.this, "上传失败！重新来一次吧！" + e.getMessage(), true);
                }
            }
        });
    }

}
