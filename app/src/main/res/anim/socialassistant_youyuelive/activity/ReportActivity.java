package com.socialassistant_youyuelive.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.OSSClient;
import com.socialassistant_youyuelive.AV.DemoApplication;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.oss.OSSUtil;
import com.socialassistant_youyuelive.oss.OssService;
import com.socialassistant_youyuelive.util.ShowToast;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;
import com.yanzhenjie.album.Album;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/14.
 */

public class ReportActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Context context;
    private TextView commit;
    private static String anchordId = "";

    private RadioGroup reason;
    private RadioButton reason_1, reason_2, reason_3, reason_4;
    private int type = -1;
    private LinearLayout put_keyBoard;
    private EditText text;
    private ImageView image;
    private String imagePath = "";
    public static final int SUCCESS = 333;
    public static final int FAIL = 444;
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS:
                    progressDialog.dismiss();
                    ShowToast.normalShow(context, "提交举报成功!", true);
                    finish();
                    break;
                case FAIL:
                    progressDialog.dismiss();
                    ShowToast.normalShow(context, "提交举报失败!", true);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        context = this;
        initToolbar(R.id.toolbar, R.id.title, "举报");
        Intent intent = getIntent();
        if (intent != null) anchordId = intent.getStringExtra("anchordId");
        initView();
    }

    private void initView() {
        reason = (RadioGroup) findViewById(R.id.reason);
        reason.setOnCheckedChangeListener(this);
        reason_1 = (RadioButton) findViewById(R.id.reason_1);
        reason_2 = (RadioButton) findViewById(R.id.reason_2);
        reason_3 = (RadioButton) findViewById(R.id.reason_3);
        reason_4 = (RadioButton) findViewById(R.id.reason_4);
        put_keyBoard = (LinearLayout) findViewById(R.id.put_keyBoard);
        put_keyBoard.setOnClickListener(this);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
        put_keyBoard.startAnimation(animation);
        text = (EditText) findViewById(R.id.text);
        image = (ImageView) findViewById(R.id.image);
        image.setOnClickListener(this);
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        commit = (TextView) findViewById(R.id.commit);
        commit.setOnClickListener(this);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    private void getAlbum(String title, int number, int code) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /*if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //已经禁止提示了
                ShowToast.show(context, R.string.NO_PERMISSION, true);
            } else {
                ActivityCompat.requestPermissions(BecomeAnchorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }*/
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            ArrayList<String> mImageList = new ArrayList<>();
            Album.album(ReportActivity.this)
                    .toolBarColor(getResources().getColor(R.color.fense)) // Toolbar color.
                    .statusBarColor(getResources().getColor(R.color.fense)) // StatusBar color.
                    .navigationBarColor(getResources().getColor(R.color.fense)) // NavigationBar color.
                    .title(title) // Title.
                    .selectCount(number) // Choose up to a few pictures.
                    .columnCount(2) // Number of albums.
                    .camera(true) // Have a camera function.
                    .checkedList(mImageList) // Has selected the picture, automatically select.
                    .start(code); // 999 is requestCode.
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commit:
                String s = text.getText().toString();
                if ((type != -1 || !TextUtils.isEmpty(s)) && !TextUtils.isEmpty(imagePath)) {
                    if (type == -1) type = 4;
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("提交举报");
                    progressDialog.setMessage("提交举报中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    ConstString.updateUserData();
                    SharedPreferences sp = context.getSharedPreferences("report", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    //举报原因类型
                    editor.putString("type", String.valueOf(type));
                    //举报附加文字
                    editor.putString("text", TextUtils.isEmpty(s) ? "" : s);
                    //举报截图地址
                    editor.putString("path", TextUtils.isEmpty(imagePath) ? "" : imagePath);
                    //举报用户
                    editor.putString("userId", ConstString.userId);
                    //被举报的主播
                    editor.putString("anchordId", anchordId);
                    //SimpleDateFormat dateformat2 = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 E ");
                    //String time = dateformat2.format(new Date());
                    //举报时间 2017年08月14日 16时18分49秒 星期一
                    //editor.putString("time", time);
                    editor.putString("time", String.valueOf(System.currentTimeMillis()));
                    editor.commit();
                    //文件oss直传上服务器
                    String bucket = OSSUtil.BUCKETNAME;
                    OSSClient oss = OssService.initOSS(this, OSSUtil.REGON_HOST, ConstString.SIGN, OSSUtil.BUCKETNAME);
                    //图片(.png)
                    String objectImage = "";
                    if (!TextUtils.isEmpty(imagePath)) {
                        String name_format = imagePath.substring(imagePath.lastIndexOf("."), imagePath.length());
                        objectImage = bucket + File.separator + ConstString.mobile + File.separator + System.currentTimeMillis() + 9 + name_format;
                    }
                    OSSUtil.asyncPutFile(oss, handler, 10, bucket, objectImage, imagePath, -1);
                } else {
                    Alerter.create(ReportActivity.this)
                            .setTitle("提交举报")
                            .setText("提交内容不充足...")
                            .setBackgroundColorRes(R.color.colorAccent)
                            // or setBackgroundColorInt(Color.CYAN)
                            .enableSwipeToDismiss()
                            .setDuration(3000)
                            .show();
                }
                break;
            case R.id.image:getAlbum("选择一张截图", 1, 666);break;
            case R.id.put_keyBoard:
                text.requestFocus();
                InputMethodManager imm = (InputMethodManager) text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        switch (i) {
            case R.id.reason_1:type = 1;break;
            case R.id.reason_2:type = 2;break;
            case R.id.reason_3:type = 3;break;
            case R.id.reason_4:type = 4;break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //头像
        if(requestCode == 666) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                ArrayList<String> imageList = Album.parseResult(data);
                String facePath = imageList.get(0);
                if (!TextUtils.isEmpty(facePath)) {
                    File file = new File(facePath);
                    /*if (file.length() > 1024 * 1024) {
                        imagePath = "";
                        ShowToast.normalShow(context, "图片大于1M,请重新选择合适的图片!", true);
                        return;
                    }*/
                    imagePath = facePath;
                    //使用最少的内存完成复杂的图片转换，转换图片以适合所显示的ImageView，来减少内存消耗
                    Picasso.with(context).load(file)
                            //裁剪图片尺寸
                            .resize(50, 50)
                            //设置图片圆角
                            .centerCrop()
                            .into(image);
                    //image.setImageBitmap(BitmapFactory.decodeFile(facePath));
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ShowToast.show(context, R.string.STORAGE_PERMISSION, true);
                }
                break;
            default:
                break;
        }
    }

}
