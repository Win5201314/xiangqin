package com.socialassistant_youyuelive.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.oss.OSSClient;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActionBarColorManager;
import com.socialassistant_youyuelive.commomentity.CircleImageView;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.oss.OSSUtil;
import com.socialassistant_youyuelive.oss.OssService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.getSDPath;
import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.saveBitmapToFile;

/**
 * Created by Administrator on 2017/5/15.
 */

public class InfoActivity extends Activity implements View.OnClickListener {

    private EditText nickName, age, signature, sign1, sexType, album, pw, clear_cache;
    private LinearLayout nickName_, age_, signature_, sign1_, sexType_, album_, pw_, clear_cache_;
    private CircleImageView face;
    private RelativeLayout relativeLayout;
    private ImageView back;
    private static String url = ConstString.URL_INFO;
    private static final int PHOTO_REQUEST_GALLERY = 1;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 2;// 结果
    private ProgressDialog progressDialog;
    public static final int SUCCESS = 11;
    public static final int FAIL = 12;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case  0:
                    progressDialog.dismiss();
                    break;
                case SUCCESS:
                    upDateUser();
                    Toast.makeText(InfoActivity.this, getString(R.string.oss_upload_success), Toast.LENGTH_LONG).show();
                    break;
                case FAIL:
                    Toast.makeText(InfoActivity.this, getString(R.string.upload_fail_oss), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    break;
            }
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_userinfo);
        ActionBarColorManager.setColor(this, getResources().getColor(R.color.fense));
        initView();
        parse(true);
    }

    private void initView() {
        relativeLayout = (RelativeLayout) findViewById(R.id.face_re);
        relativeLayout.setOnClickListener(this);
        face = (CircleImageView) findViewById(R.id.face);
        face.setOnClickListener(this);
        nickName = (EditText) findViewById(R.id.nickName);
        nickName.setOnClickListener(this);
        age = (EditText) findViewById(R.id.age);
        age.setOnClickListener(this);
        signature = (EditText) findViewById(R.id.signature);
        signature.setOnClickListener(this);
        sign1 = (EditText) findViewById(R.id.sign1);
        sign1.setOnClickListener(this);
        sexType = (EditText) findViewById(R.id.sex);
        sexType.setOnClickListener(this);
        album = (EditText) findViewById(R.id.album);
        album.setOnClickListener(this);
        pw = (EditText) findViewById(R.id.pw);
        pw.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        clear_cache = (EditText) findViewById(R.id.clear_cache);
        clear_cache.setOnClickListener(this);
        nickName_ = (LinearLayout) findViewById(R.id.nickName_);
        nickName_.setOnClickListener(this);
        age_ = (LinearLayout) findViewById(R.id.age_);
        age_.setOnClickListener(this);
        sign1_ = (LinearLayout) findViewById(R.id.sign1_);
        sign1_.setOnClickListener(this);
        signature_ = (LinearLayout) findViewById(R.id.signature_);
        signature_.setOnClickListener(this);
        sexType_ = (LinearLayout) findViewById(R.id.sex_);
        sexType_.setOnClickListener(this);
        album_ = (LinearLayout) findViewById(R.id.album_);
        album_.setOnClickListener(this);
        pw_ = (LinearLayout) findViewById(R.id.pw_);
        pw_.setOnClickListener(this);
        clear_cache_ = (LinearLayout) findViewById(R.id.clear_cache_);
        clear_cache_.setOnClickListener(this);
    }

    private void parse(boolean fa) {
        JSONObject jo = JSON.parseObject(ConstString.user);
        if (jo != null) {
            String s = jo.getString("headImgUrl");
            if (s != null && !s.equals("")) {
                if (fa) ImageLoader.getInstance().displayImage(s, face);
            }
            s = jo.getString("nickName");
            if (s != null && !s.trim().equals("")) {
                nickName.setHint(getString(R.string.nick_name_) + s);
            } else {
                nickName.setHint(getString(R.string.nick_name_) + ConstString.mobile);
            }
            s = jo.getString("signature");
            if (s != null && !s.trim().equals("")) {
                signature.setHint(getString(R.string.sign_) + s);
            }
            if (ConstString.isLiver) {
                sign1.setVisibility(View.VISIBLE);
                s = jo.getString("labels");
                if (s != null && !s.trim().equals("")) {
                    String[] la = s.split(",");
                    s = "";
                    for (String t : la) s = s + t + " ";
                    if (!s.trim().equals("")) sign1.setHint(s);
                }
            } else {
                sign1.setVisibility(View.GONE);
            }
            s = jo.getString("age");
            if (s != null && !s.trim().equals("") && isNumber(s)) {
                age.setHint(getString(R.string.age_) + s);
            } else {
                age.setHint(getString(R.string.age_) + "23");
            }
            int SEX  = jo.getBoolean("sex") ? 1 : 0;
            if (SEX == 0) {//女
                sexType.setHint(getString(R.string.girl));
            } else {//男
                sexType.setHint(getString(R.string.boy));
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        parse(false);
    }

    private boolean isNumber(String year) {
        if (year.matches("[0-9]+")) {
            int age = Integer.parseInt(year);
            if (age > 0 && age < 100) return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.face_re:
                choosePhoto();
                break;
            case R.id.face:
                choosePhoto();
                break;
            case R.id.nickName_:
                startActivity(new Intent(InfoActivity.this, ChangeNickNameActivity.class));
                break;
            case R.id.age_:
                startActivity(new Intent(InfoActivity.this, ChangeAgeActivity.class));
                break;
            case R.id.sign1_:
                startActivity(new Intent(InfoActivity.this, ChangeTagActivity.class));
                break;
            case R.id.signature_:
                startActivity(new Intent(InfoActivity.this, ChangeSignatureActivity.class));
                break;
            case R.id.sex_:
                startActivity(new Intent(InfoActivity.this, ChangeSexTypeActivity.class));
                break;
            case R.id.album_:
                //startActivity(new Intent(InfoActivity.this, ChangeAlbumActivity.class));
                break;
            case R.id.pw_:
                startActivity(new Intent(InfoActivity.this, ChangePWActivity.class));
                break;
            case R.id.clear_cache_:
                deleteDir();
                Toast.makeText(InfoActivity.this, getResources().getString(R.string.clear_cache_success), Toast.LENGTH_LONG).show();
                break;
        }
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir() {
        File dir = new File(getSDPath() + ConstString.PATH_CACHE + File.separator);
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;

        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDir(); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    private void uploadFace(Bitmap bitmap) {
        String pathImage = getSDPath() + "/youyue/" + 0 + ".png";
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
        //上传1张头像图片
        File fileImage = new File(pathImage);
        if (fileImage != null && fileImage.exists()) {
            String objectImage = bucket + File.separator + ConstString.mobile + File.separator + 0 + ".png";
            OSSUtil.asyncPutFile(oss, handler, 6, bucket, objectImage, pathImage, -1);
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
                face.setImageBitmap(bitmap);
                progressDialog = new ProgressDialog(InfoActivity.this);
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
                            Toast.makeText(InfoActivity.this, getString(R.string.submit_fail), Toast.LENGTH_LONG).show();
                            return;
                        }
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ConstString.user = jsonObject.getString("user");
                            Toast.makeText(InfoActivity.this, getString(R.string.info_success), Toast.LENGTH_LONG).show();
                            parse(true);
                        } else {
                            Toast.makeText(InfoActivity.this, getString(R.string.submit_fail), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(0);
                Toast.makeText(InfoActivity.this, getString(R.string.submit_fail), Toast.LENGTH_LONG).show();
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
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
