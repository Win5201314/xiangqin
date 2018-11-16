package com.socialassistant_youyuelive.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.oss.OSSClient;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.socialassistant_youyuelive.PCLocation.AddressPickTask;
import com.socialassistant_youyuelive.PCLocation.City;
import com.socialassistant_youyuelive.PCLocation.County;
import com.socialassistant_youyuelive.PCLocation.Province;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActionBarColorManager;
import com.socialassistant_youyuelive.commomentity.CircleImageView;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.RoundImageView;
import com.socialassistant_youyuelive.commomentity.RoundProgressBar;
import com.socialassistant_youyuelive.oss.OSSUtil;
import com.socialassistant_youyuelive.oss.OssService;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.getSDPath;
import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.saveBitmapToFile;

/**
 * Created by Administrator on 2017/5/27.
 */

public class MeActivity extends LeftSlipActivity implements View.OnClickListener {

    private ImageView bg, back, sex;
    private RoundImageView head;
    private TextView nickName, nickName_, sex_, birthday, signature, tag;
    private TextView province_city;
    private RoundImageView pic_1, pic_2, pic_3, pic_4, pic_5;
    private RelativeLayout face_re, nc, sex_re, bir_re,signature_re, tag_re, pw_re, clear;
    private CircleImageView face;
    private static String url = ConstString.URL_INFO;
    private static final int PHOTO_REQUEST_GALLERY = 1;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 2;// 结果
    private ProgressDialog progressDialog;
    public static final int SUCCESS = 11;
    public static final int FAIL = 12;
    private Context context;
    private DisplayImageOptions options;
    private View last;
    private String[] pic_urls = new String[5];

    private LinearLayout ly;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarColorManager.ImmersiveStatusBar(this);
        setContentView(R.layout.activity_me);
        context = this;
        animation = AnimationUtils.loadAnimation(context, R.anim.en);
        //显示图片的配置
        options = new DisplayImageOptions.Builder()
                /*.showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error)*/
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        initView();
        parseUser();

        //SearchIDActivity.animation(600);
        //LayoutAnimationController controller = new LayoutAnimationController(SearchIDActivity.set, 1);
        ly = (LinearLayout) findViewById(R.id.ly);
        //ly.setLayoutAnimation(controller);
        //ly.startAnimation(animation);

        bg.startAnimation(animation);
        head.startAnimation(animation);

        sharedPreferences = getSharedPreferences("Login_state", Context.MODE_PRIVATE);
        ConstString.API_KEY = sharedPreferences.getString("token", "");
        ConstString.KEY = sharedPreferences.getString("key", "");
        ConstString.isLiver = sharedPreferences.getBoolean("isLiver", false);

        requestPermissionAndroid_6();
    }

    //对外部存储读写权限的申请
    private void requestPermissionAndroid_6() {
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MeActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ShowToast.show(context, R.string.NO_PERMISSION, true);
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case  0:
                    progressDialog.dismiss();
                    break;
                case SUCCESS:
                    upDateUser();
                    ShowToast.show(context, R.string.oss_upload_success, true);
                    break;
                case FAIL:
                    ShowToast.show(context, R.string.upload_fail_oss, true);
                    progressDialog.dismiss();
                    break;
            }
        }

    };


    private void initView() {
        bg = (ImageView) findViewById(R.id.bg);
        bg.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        sex = (ImageView) findViewById(R.id.sex);
        sex_ = (TextView) findViewById(R.id.sex_);
        head = (RoundImageView) findViewById(R.id.head);
        head.setOnClickListener(this);
        nickName = (TextView) findViewById(R.id.nickName);
        nickName_ = (TextView) findViewById(R.id.nickName_);
        province_city = (TextView) findViewById(R.id.province_city);
        province_city.setOnClickListener(this);
        birthday = (TextView) findViewById(R.id.birthday);
        signature = (TextView) findViewById(R.id.signature);
        tag = (TextView) findViewById(R.id.tag);
        //根据各自的构造方法来初始化一个实例对象
        pic_1 = (RoundImageView) findViewById(R.id.pic_1);
        pic_2 = (RoundImageView) findViewById(R.id.pic_2);
        pic_3 = (RoundImageView) findViewById(R.id.pic_3);
        pic_4 = (RoundImageView) findViewById(R.id.pic_4);
        pic_5 = (RoundImageView) findViewById(R.id.pic_5);
        pic_1.setOnClickListener(this);
        pic_2.setOnClickListener(this);
        pic_3.setOnClickListener(this);
        pic_4.setOnClickListener(this);
        pic_5.setOnClickListener(this);
        face_re = (RelativeLayout) findViewById(R.id.face_re);
        face_re.setOnClickListener(this);
        face = (CircleImageView) findViewById(R.id.face);
        face.setOnClickListener(this);
        nc = (RelativeLayout) findViewById(R.id.nc);
        nc.setOnClickListener(this);
        sex_re = (RelativeLayout) findViewById(R.id.sex_re);
        sex_re.setOnClickListener(this);
        bir_re = (RelativeLayout) findViewById(R.id.bir_re);
        bir_re.setOnClickListener(this);
        signature_re = (RelativeLayout) findViewById(R.id.signature_re);
        signature_re.setOnClickListener(this);
        tag_re = (RelativeLayout) findViewById(R.id.tag_re);
        tag_re.setOnClickListener(this);
        last = findViewById(R.id.last);
        pw_re = (RelativeLayout) findViewById(R.id.pw_re);
        pw_re.setOnClickListener(this);
        clear = (RelativeLayout) findViewById(R.id.clear);
        clear.setOnClickListener(this);
        showAnimation();
    }

    //动画
    void showAnimation() {
        pic_1.startAnimation(animation);
        pic_2.startAnimation(animation);
        pic_3.startAnimation(animation);
        pic_4.startAnimation(animation);
        pic_5.startAnimation(animation);
    }

    private void parseUser() {
        JSONObject jo = JSON.parseObject(ConstString.user);
        if (jo != null) {
            String s = jo.getString("headImgUrl");
            if (s != null && !s.equals("")) {
                ImageLoader.getInstance().displayImage(s, face, options);
                ImageLoader.getInstance().displayImage(s, head, options);
            }
            s = jo.getString("album");
            if (s != null && !s.equals("")) {
                //显示图片的配置
                options = new DisplayImageOptions.Builder()
                /*.showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error)*/
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .build();
                if (ConstString.isLiver) {
                    pic_urls = s.split(",");
                    if (pic_urls != null && pic_urls.length > 0) {
                        for (int i = 0; i < pic_urls.length; i++) {
                            String url_ = pic_urls[i];
                            if (url_ == null || url_.equals("")) continue;
                            switch (i) {
                                case 0:
                                    ImageLoader.getInstance().displayImage(url_, pic_1, options);
                                    ImageLoader.getInstance().displayImage(url_, bg, options);
                                    break;
                                case 1:
                                    ImageLoader.getInstance().displayImage(url_, pic_2, options);
                                    break;
                                case 2:
                                    ImageLoader.getInstance().displayImage(url_, pic_3, options);
                                    break;
                                case 3:
                                    ImageLoader.getInstance().displayImage(url_, pic_4, options);
                                    break;
                                case 4:
                                    ImageLoader.getInstance().displayImage(url_, pic_5, options);
                                    break;
                            }
                        }
                    }
                } else {
                    Log.d("R", s);
                    Log.d("R", "------------------------------------------------");
                    JSONArray array = JSON.parseArray(s);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            String url_ = array.get(i).toString();
                            pic_urls[i] = url_;
                            if (url_ == null || url_.equals("")) continue;
                            switch (i) {
                                case 0:
                                    ImageLoader.getInstance().displayImage(url_, pic_1, options);
                                    ImageLoader.getInstance().displayImage(url_, bg, options);
                                    break;
                                case 1:
                                    ImageLoader.getInstance().displayImage(url_, pic_2, options);
                                    break;
                                case 2:
                                    ImageLoader.getInstance().displayImage(url_, pic_3, options);
                                    break;
                                case 3:
                                    ImageLoader.getInstance().displayImage(url_, pic_4, options);
                                    break;
                                case 4:
                                    ImageLoader.getInstance().displayImage(url_, pic_5, options);
                                    break;
                            }
                        }
                    }
                }
            }
            s = jo.getString("nickName");
            if (s != null && !s.trim().equals("")) {
                nickName.setText(s);
                nickName_.setText(s);
            } else {
                nickName.setText(ConstString.mobile);
                nickName_.setText(ConstString.mobile);
            }
            s = jo.getString("signature");
            if (s != null && !s.trim().equals("")) {
                signature.setText(s);
            }
            if (ConstString.isLiver) {
                tag_re.setVisibility(View.VISIBLE);
                s = jo.getString("labels");
                if (s != null && !s.trim().equals("")) {
                    String[] la = s.split(",");
                    s = "";
                    String s1 = "", s2 = "", s3 = "";
                    for (int i = 0; i < la.length; i++) {
                        switch (i) {
                            case 0:
                                s1 = la[0];
                                break;
                            case 1:
                                s2 = la[1];
                                break;
                            case 2:
                                s3 = la[2];
                                break;
                        }
                    }
                    //if (!s.trim().equals("")) tag.setText(s);
                    String newMessageInfo = "<font color='red'><b>" + s1 + " " + "</b></font>"
                            + "<font color='yellow'><b>" + s2 + " " + "</b></font>"
                            + "<font color='green'><b>" + s3 + " " + "</b></font>";
                    tag.setText(Html.fromHtml(newMessageInfo));
                } else {
                    tag.setText("");
                }
            } else {
                tag_re.setVisibility(View.GONE);
                last.setVisibility(View.GONE);
            }
            s = jo.getString("birthday");
            if (s != null && !s.trim().equals("")) {
                birthday.setText(s);
            } else {
                birthday.setText("");
            }
            String province = jo.getString("province");
            String city = jo.getString("city");
            if (province != null && !province.equals("")
                    && city != null && !city.equals("")) {
            } else {
                province = "广东";
                city = "深圳";
            }
            province_city.setText(province + "," + city);
            int SEX  = jo.getBooleanValue("sex") ? 1 : 0;
            if (SEX == 0) {//女
                sex_.setText(getString(R.string.girl));
                sex.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.anchor_sex_presed));
            } else {//男
                sex_.setText(getString(R.string.boy));
                sex.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.man));
            }
        }
    }

    private void showBigPicture(String url, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null); // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final RoundProgressBar progressbar = (RoundProgressBar) imgEntryView.findViewById(R.id.roundProgressBar);
        ImageView img = (ImageView)imgEntryView.findViewById(R.id.large_image);
        img.startAnimation(animation);
        if (url == null || url.equals("")) {
            /*img.setImageBitmap(bitmap);
            progressbar.setVisibility(View.GONE);*/
        } else {
            ImageLoader.getInstance().displayImage(url, img, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    //开始加载的时候执行
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    //加载失败的时候执行
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //加载成功的时候执行
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    //加载取消的时候执行
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    //在这里更新 ProgressBar的进度信息
                    //设置进度条图片的总大小
                    progressbar.setMax(total);
                    // 设置当前加载进度
                    progressbar.setProgress(current);
                    if (current == total) {
                        progressbar.setVisibility(View.GONE);
                    }
                }
            });
        }
        dialog.setView(imgEntryView); // 自定义dialog
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
        switch (i) {
            case 1:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 2:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 3:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 4:
                window.setWindowAnimations(R.style.mystyle_2);  //添加动画
                break;
            case 0:
                window.setWindowAnimations(R.style.mystyle_3);  //添加动画
                break;
        }
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                dialog.cancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("Login_state", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();//获取编辑器
        editor.putString("user", ConstString.user);
        editor.commit();
        parseUser();
        showAnimation();
    }

    public void onAddress3Picker() {
        AddressPickTask task = new AddressPickTask(this);
        task.setHideCounty(true);
        task.setCallback(new AddressPickTask.Callback() {
            @Override
            public void onAddressInitFailed() {
                ShowToast.normalShow(context, "数据初始化失败", true);
            }

            @Override
            public void onAddressPicked(Province province, City city, County county) {
                province_city.setText(province.getAreaName() + "," + city.getAreaName());
                updateLocation(province.getAreaName(), city.getAreaName());
                //Toast.makeText(context, province.getAreaName() + " " + city.getAreaName(), Toast.LENGTH_SHORT).show();
            }
        });
        task.execute("广东省", "深圳市");
    }

    private Map<String, String> params = new HashMap<>();
    private void updateLocation(final String province, final String city) {
        // TODO Auto-generated method stub
        params.put("province", province);
        params.put("city", city);
        if (ConstString.isLiver) {
            params.put("userId", ConstString.anchor_id);
        } else {
            params.put("userId", ConstString.userId);
        }
        params.put("time", String.valueOf(System.currentTimeMillis()));
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                ConstString.URL_INFO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) return;
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ConstString.user = jsonObject.getString("user");
                            ShowToast.show(context, R.string.info_success, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                ShowToast.show(context, R.string.access_fail, true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                /*Map<String, String> params = new HashMap<>();
                params.put("province", province);
                params.put("city", city);
                if (ConstString.isLiver) {
                    params.put("userId", ConstString.anchor_id);
                } else {
                    params.put("userId", ConstString.userId);
                }*/
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> pa = new HashMap<>();
                String sign = HttpUtil.createSign(params, ConstString.API_KEY);
                pa.put("sign", sign);
                pa.put("key", ConstString.KEY);
                return pa;
            }
        };
        mQueue.add(postRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.province_city:
                onAddress3Picker();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.bg:
                break;
            case R.id.head:
                JSONObject jo = JSON.parseObject(ConstString.user);
                if (jo != null) {
                    String s = jo.getString("headImgUrl");
                    if (s != null && !s.equals("")) {
                        showBigPicture(s, 1);
                    }
                }
                break;
            case R.id.pic_1:
                if (ConstString.isLiver) {
                    BigShow(0);
                    /*if (pic_urls[0] != null && !pic_urls[0].equals(""))
                        showBigPicture(pic_urls[0], 0);*/
                } else {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    if (pic_urls[0] != null && !pic_urls[0].equals(""))
                        intent.putExtra("img_url", pic_urls[0]);
                    intent.putExtra("num", 1);
                    startActivity(intent);
                }
                break;
            case R.id.pic_2:
                if (ConstString.isLiver) {
                    BigShow(1);
                    /*if (pic_urls[1] != null && !pic_urls[1].equals(""))
                        showBigPicture(pic_urls[1], 1);*/
                } else {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    if (pic_urls[1] != null && !pic_urls[1].equals(""))
                        intent.putExtra("img_url", pic_urls[1]);
                    intent.putExtra("num", 2);
                    startActivity(intent);
                }
                break;
            case R.id.pic_3:
                if (ConstString.isLiver) {
                    BigShow(2);
                    /*if (pic_urls[2] != null && !pic_urls[2].equals(""))
                        showBigPicture(pic_urls[2], 2);*/
                } else {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    if (pic_urls[2] != null && !pic_urls[2].equals(""))
                        intent.putExtra("img_url", pic_urls[2]);
                    intent.putExtra("num", 3);
                    startActivity(intent);
                }
                break;
            case R.id.pic_4:
                if (ConstString.isLiver) {
                    BigShow(3);
                    /*if (pic_urls[3] != null && !pic_urls[3].equals(""))
                        showBigPicture(pic_urls[3], 3);*/
                } else {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    if (pic_urls[3] != null && !pic_urls[3].equals(""))
                        intent.putExtra("img_url", pic_urls[3]);
                    intent.putExtra("num", 4);
                    startActivity(intent);
                }
                break;
            case R.id.pic_5:
                if (ConstString.isLiver) {
                    BigShow(4);
                    /*if (pic_urls[4] != null && !pic_urls[4].equals(""))
                        showBigPicture(pic_urls[4], 4);*/
                } else {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    if (pic_urls[4] != null && !pic_urls[4].equals(""))
                        intent.putExtra("img_url", pic_urls[4]);
                    intent.putExtra("num", 5);
                    startActivity(intent);
                }
                break;
            case R.id.face_re:
                startActivity(new Intent(context, HeadActivity.class));
                break;
            case R.id.face:
                startActivity(new Intent(context, HeadActivity.class));
                break;
            case R.id.nc:
                startActivity(new Intent(context, ChangeNickNameActivity.class));
                break;
            case R.id.sex_re:
                startActivity(new Intent(context, ChangeSexTypeActivity.class));
                break;
            case R.id.bir_re:
                startActivity(new Intent(context, ChangeAgeActivity.class));
                break;
            case R.id.signature_re:
                startActivity(new Intent(context, ChangeSignatureActivity.class));
                break;
            case R.id.tag_re:
                startActivity(new Intent(context, ChangeTagActivity.class));
                break;
            case R.id.pw_re:
                startActivity(new Intent(context, ChangePWActivity.class));
                break;
            case R.id.clear:
                deleteDir();
                deleteDir_();
                ShowToast.show(context, R.string.clear_cache_success, true);
                break;
        }
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir() {
        File dir = new File(getSDPath() + ConstString.PATH_AUDIO + File.separator);
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile()) file.delete(); // 删除所有文件
            else if (file.isDirectory()) deleteDir(); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
    public static void deleteDir_() {
        File dir = new File(getSDPath() + ConstString.PA + File.separator);
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile()) file.delete(); // 删除所有文件
            else if (file.isDirectory()) deleteDir_(); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    private void BigShow(int which) {
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < pic_urls.length; i++) {
            if (pic_urls[i] != null && pic_urls[i].length() > 0)
            urls.add(pic_urls[i]);
        }
        imageBrower(which, urls);
    }

    /**
     * 打开图片查看器
     *
     * @param position
     * @param urls2
     */
    protected void imageBrower(int position, ArrayList<String> urls2) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls2);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
        startActivity(intent);
    }

    private AlertDialog dialog;
    private static final int TAKE_PICTURE = 3;//拍照获取
    private void changeBackgroud() {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.change_bg_1, null);
        TextView album_bg = (TextView) view.findViewById(R.id.album_bg);
        album_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        TextView take_photo_bg = (TextView) view.findViewById(R.id.take_photo_bg);
        take_photo_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开本地系统相机
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCameraIntent, TAKE_PICTURE);
            }
        });
        TextView cancel_bg = (TextView) view.findViewById(R.id.cancel_bg);
        cancel_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = new AlertDialog.Builder(context)
                /*.setTitle(getResources().getString(R.string.app_name))*/
                /*.setMessage("message")*/
                .setView(view)
                .create();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        WindowManager.LayoutParams windowparams = window.getAttributes();
        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        //int height = display.getHeight();
        int width = display.getWidth();
        windowparams.width = width;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(
                (android.view.WindowManager.LayoutParams) windowparams);
        /*WindowManager.LayoutParams lp = window.getAttributes();
        // 设置透明度为0.3
        lp.alpha = 0.5f;
        *//*我们在使用某些应用时会发现当弹出对话框或某些模式窗口时，后面的内容会变得模糊或不清楚。
        实际上，这些效果也很容易在OPhone中实现。为了实现这个功能，
        我们只需要设置Wndow对象的两个标志即可，代码如下：*//*
        *//*window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);*//*
        window.setAttributes(lp);*/
        dialog.show();
    }

    private void uploadFace(Bitmap bitmap) {
        String pathImage = getSDPath() + "/youyue/" + 6 + ".png";
        try {
            saveBitmapToFile(bitmap, pathImage);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(FAIL);
            return;
        }
        //文件oss直传上服务器
        String bucket = OSSUtil.BUCKETNAME;
        OSSClient oss = OssService.initOSS(this, OSSUtil.REGON_HOST, ConstString.SIGN, OSSUtil.BUCKETNAME);
        //上传1张背景图片
        File fileImage = new File(pathImage);
        if (fileImage != null && fileImage.exists()) {
            String objectImage = bucket + File.separator + ConstString.mobile + File.separator + 6 + ".png";
            OSSUtil.asyncPutFile(oss, handler, 4, bucket, objectImage, pathImage, -1);
        }
    }

    private void choosePhoto() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    //剪切图片
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private Uri uri;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                uri = data.getData();
                crop(uri);
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                final Bitmap bitmap = data.getParcelableExtra("data");
                if (bitmap == null) return;
                /*
                String[] proj = {MediaStore.Images.Media.DATA};
                //好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                //按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                //最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                if (path == null || path.equals("")) return;
                //Toast.makeText(InfoActivity.this, path, Toast.LENGTH_LONG).show();
                */
                bg.setImageBitmap(bitmap);
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(getString(R.string.app_name));
                progressDialog.setMessage(getString(R.string.uploading_user));
                progressDialog.setCancelable(true);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFace(bitmap);
                    }
                }).start();
            }
        } else if (requestCode == TAKE_PICTURE) {
            if (data != null) {
                final Bitmap bm = (Bitmap) data.getExtras().get("data");
                if (bm == null) return;
                bg.setImageBitmap(bm);
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(getString(R.string.app_name));
                progressDialog.setMessage(getString(R.string.uploading_user));
                progressDialog.setCancelable(true);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFace(bm);
                    }
                }).start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upDateUser() {
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
                            ShowToast.show(context, R.string.submit_fail, true);
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ConstString.user = jsonObject.getString("user");
                            ShowToast.show(context, R.string.info_success, true);
                            sharedPreferences = getSharedPreferences("Login_state", Context.MODE_PRIVATE);
                            editor = sharedPreferences.edit();//获取编辑器
                            editor.putString("user", ConstString.user);
                            editor.commit();
                            //parse(true);
                        } else {
                            ShowToast.show(context, R.string.submit_fail, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(0);
                ShowToast.show(context, R.string.submit_fail, true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (ConstString.isLiver) {
                    params.put("userId", ConstString.anchor_id);
                } else {
                    params.put("userId", ConstString.userId);
                }
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(v1 >= 5) {
                //此为自定义的动画效果，下面两个为系统的动画效果
                overridePendingTransition(R.anim.en, R.anim.ex);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
