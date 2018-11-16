package com.socialassistant_youyuelive.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.oss.OSSClient;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.socialassistant_youyuelive.AV.md5.MD5;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActionSheetDialog;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.RoundProgressBar;
import com.socialassistant_youyuelive.commomentity.UserData;
import com.socialassistant_youyuelive.oss.OSSUtil;
import com.socialassistant_youyuelive.oss.OssService;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;
import com.squareup.picasso.Picasso;
import com.yanzhenjie.album.Album;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.getSDPath;
import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.saveBitmapToFile;

/**
 * Created by Administrator on 2017/5/31.
 */

public class HeadActivity extends BaseActivity implements View.OnClickListener {

    private TextView keep;
    private ImageView head;
    private LinearLayout root;
    private static final String url = ConstString.IP + "/video/user/getUserIdMessage";
    private ProgressDialog progressDialog;
    private RoundProgressBar progressbar;
    private static final int PHOTO_REQUEST_GALLERY = 1;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 2;// 结果
    private static final int TAKE_PICTURE = 3;//拍照获取
    public static final int SUCCESS = 13;
    public static final int FAIL = 14;
    public static int img_progress = 0;
    public static final int PROGRESS_HEAD = 15;
    private Context context;
    private Bitmap bitmap = null;
    //显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder()
                /*.showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error)*/
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    private ActionSheetDialog actionSheetDialog = null;
    private Animation animation;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
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
                case PROGRESS_HEAD:
                    progressDialog.setProgress(img_progress);
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head);
        initToolbar(R.id.toolbar, R.id.title, "头像");
        head = (ImageView) findViewById(R.id.head);
        progressbar = (RoundProgressBar) findViewById(R.id.roundProgressBar);
        context = this;
        JSONObject jo = JSON.parseObject(ConstString.user);
        if (jo != null) {
            String headImgUrl = jo.getString("headImgUrl");
            if (!TextUtils.isEmpty(headImgUrl)) {
                ImageLoader.getInstance().displayImage(headImgUrl, head, options, new ImageLoadingListener() {
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
            } else {
                progressbar.setVisibility(View.GONE);
            }
        }

        animation = AnimationUtils.loadAnimation(context, R.anim.en);
        head.startAnimation(animation);

        root = (LinearLayout) findViewById(R.id.root);
        root.setOnClickListener(this);

        showAcionSheetDialog();

        ConstString.updateUserData();
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        keep = (TextView) findViewById(R.id.keep);
        keep.setOnClickListener(this);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    //对外部存储读写权限的申请
    private void requestPermissionREAD_WRITEAndroid_6() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /*if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //已经禁止提示了
                ShowToast.show(context, R.string.NO_PERMISSION, true);
            } else {
                ActivityCompat.requestPermissions(HeadActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }*/
            ActivityCompat.requestPermissions(HeadActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            //choosePhoto();
            getPicture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //choosePhoto();
                    openAlbum();
                } else {
                    ShowToast.show(context, R.string.STORAGE_PERMISSION, true);
                }
                break;
            default:
                break;
        }
    }

    private void showAcionSheetDialog() {
        if (Build.VERSION.SDK_INT >= 24) {
            actionSheetDialog = new ActionSheetDialog(context)
                    .builder()
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .addSheetItem("从相册中选择", ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    //choosePhoto();
                                    //requestPermissionREAD_WRITEAndroid_6();
                                    //getAlbum("选择一张图片或者拍照", 1, 666);
                                    if (ContextCompat.checkSelfPermission(HeadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(HeadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    } else {
                                        openAlbum();
                                    }
                                }
                            })
                    .addSheetItem("拍照", ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    takePhoto();
                                }
                    });
            actionSheetDialog.show();
        } else {
            actionSheetDialog = new ActionSheetDialog(context)
                    .builder()
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .addSheetItem("从相册中选择或者拍照", ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    //choosePhoto();
                                    //requestPermissionREAD_WRITEAndroid_6();
                                    getAlbum("选择一张图片或者拍照", 1, 666);
                                }
                            });
                /*.addSheetItem("拍照", ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                takePhoto();
                            }
                });*/
            actionSheetDialog.show();
        }
    }

    private ArrayList<String> mImageList = new ArrayList<>();
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
            ActivityCompat.requestPermissions(HeadActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Album.album(HeadActivity.this)
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep:
                /*if (bitmap != null) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.app_name));
                    progressDialog.setMessage(getResources().getString(R.string.submit_));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFace(bitmap);
                        }
                    }).start();
                } else {
                    AlbumActivity.ShowAlerter(HeadActivity.this);
                }*/
                if (!TextUtils.isEmpty(facePath)) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.app_name));
                    progressDialog.setMessage(getResources().getString(R.string.submit_));
                    progressDialog.setCancelable(true);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setProgress(img_progress);
                    progressDialog.show();
                    //文件oss直传上服务器
                    String bucket = OSSUtil.BUCKETNAME;
                    ConstString.SIGN = OSSUtil.STSGET_SERVER + "?mobile=" + ConstString.mobile + "&sign=" + MD5.getStringMD5("youyue" + ConstString.mobile);
                    OSSClient oss = OssService.initOSS(this, OSSUtil.REGON_HOST, ConstString.SIGN, OSSUtil.BUCKETNAME);
                    //上传1张头像图片
                    File fileImage = new File(facePath);
                    if (fileImage != null && fileImage.exists()) {
                        String objectImage = bucket + File.separator + ConstString.mobile + File.separator + System.currentTimeMillis() + ".png";
                        OSSUtil.asyncPutFile(oss, handler, 6, bucket, objectImage, facePath, -1);
                    }
                } else {
                    AlbumActivity.ShowAlerter(HeadActivity.this);
                }
                break;
            case R.id.album:
                choosePhoto();
                break;
            /*case R.id.take_photo:
                takePhoto();
                break;
            case R.id.showDialog:
                showAcionSheetDialog();
                break;*/
            case R.id.root:
                showAcionSheetDialog();
                break;
        }
    }

    private Uri imageUri;
    private void takePhoto() {
        if (bitmap != null) bitmap.recycle();
        if (Build.VERSION.SDK_INT >= 24) {
            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageUri = FileProvider.getUriForFile(HeadActivity.this, "com.example.cameraalbumtest.fileprovider", outputImage);
        }/* else {
            imageUri = Uri.fromFile(outputImage);
        }*/
        //启动相机程序
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= 24) openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    private void  choosePhoto() {
        if (bitmap != null) bitmap.recycle();
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
        //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
        int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
        if(v1 >= 5) {
            //此为自定义的动画效果
            overridePendingTransition(R.anim.en, R.anim.ex);
        }
    }

    private static final int SELECT_PICTURE = 10;
    private void getPicture() {
        if (bitmap != null) bitmap.recycle();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
        //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
        int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
        if(v1 >= 5) {
            //此为自定义的动画效果
            overridePendingTransition(R.anim.en, R.anim.ex);
        }
    }

    private static final int CHOOSE_ALBUM = 132;
    private void openAlbum() {
        if (bitmap != null) bitmap.recycle();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), CHOOSE_ALBUM);
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
        intent.putExtra("outputX", ConstString.WIDTH);
        intent.putExtra("outputY", ConstString.HEIGHT);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private Uri uri;
    private static String facePath = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //头像
        if(requestCode == 666) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                ArrayList<String> imageList = Album.parseResult(data);
                facePath = imageList.get(0);
                if (!TextUtils.isEmpty(facePath)) {
                    File file = new File(facePath);
                    /*if (file.length() > 1024 * 1024) {
                        facePath = "";
                        ShowToast.normalShow(context, "图片大于1M,请重新选择合适的图片!", true);
                        return;
                    }*/
                    //加载本地文件
                    Picasso.with(context).load(file).into(head);
                    //head.setImageBitmap(BitmapFactory.decodeFile(facePath));
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
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
                Bitmap bm = data.getParcelableExtra("data");
                if (bm == null) return;
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
                head.setImageBitmap(bm);
                bitmap = bm;
            }
        } else if (requestCode == TAKE_PICTURE) {
            if (Build.VERSION.SDK_INT >= 24) {
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                        Picasso.with(context).load(outputImage)
                                .resize(400, 400)
                                .centerCrop()
                                .into(head);
                        //head.setImageBitmap(bm);
                        facePath = outputImage.getAbsolutePath();
                        bitmap = bm;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Bitmap bm = (Bitmap) bundle.get("data");
                        if (bm == null) return;
                        head.setImageBitmap(bm);
                        bitmap = bm;
                    } else {
                        ShowToast.normalShow(context, "获取图片失败，请重试!", true);
                    }
                }
            }
        } else if (requestCode == SELECT_PICTURE) {
            if(resultCode == RESULT_OK){
                //选择图片
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                try {
                    if(bitmap != null)//如果不释放的话，不断取图片，将会内存不够
                        bitmap.recycle();
                    Bitmap bmp = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    head.setImageBitmap(bmp);
                    bitmap = bmp;
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                ShowToast.normalShow(context, "请重新选择图片", true);
            }
        }
        if ( requestCode == CHOOSE_ALBUM) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= 19) {
                    handleImageOnKitKat(data);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri，则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            /*Picasso.with(context).load(imagePath)
                    //.resize(200, 200)
                    .centerCrop()
                    .into(head);*/
            facePath = imagePath;
            if (bitmap != null) {
                ShowToast.normalShow(context, "图片如果没有显示，请直接点击保存！", true);
                head.setImageBitmap(bitmap);
            }
        } else {
            ShowToast.normalShow(context, "获取图片失败!", true);
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
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
        ConstString.SIGN = OSSUtil.STSGET_SERVER + "?mobile=" + ConstString.mobile + "&sign=" + MD5.getStringMD5("youyue" + ConstString.mobile);
        OSSClient oss = OssService.initOSS(this, OSSUtil.REGON_HOST, ConstString.SIGN, OSSUtil.BUCKETNAME);
        //上传1张头像图片
        File fileImage = new File(pathImage);
        if (fileImage != null && fileImage.exists()) {
            String objectImage = bucket + File.separator + ConstString.mobile + File.separator + 0 + ".png";
            OSSUtil.asyncPutFile(oss, handler, 6, bucket, objectImage, pathImage, -1);
        }
    }

    private Map<String, String> params = new HashMap<>();
    private void upDateUser() {
        ConstString.updateUserData();
        if (ConstString.isLiver) {
            params.put("userId", ConstString.anchor_id);
        } else {
            params.put("userId", ConstString.userId);
        }
        params.put("time", String.valueOf(System.currentTimeMillis()));
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
                            UserData userData = DataSupport.findFirst(UserData.class);
                            if (userData != null) {
                                userData.setUserData(ConstString.user);
                                userData.save();
                            }
                            //清除缓存
                            deleteDir();
                            finish();
                        } else {
                            ShowToast.show(context, R.string.submit_fail, true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(0);
                if (error instanceof NetworkError) {
                    ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                } else if (error instanceof ServerError) {
                    ShowToast.normalShow(context, "服务器繁忙，请稍后重试!", true);
                } else if (error instanceof AuthFailureError) {
                    ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                } else if (error instanceof NoConnectionError) {
                    ShowToast.normalShow(context, "本地网络链接异常!", true);
                } else if (error instanceof TimeoutError) {
                    ShowToast.normalShow(context, "访问服务器超时!", true);
                } else {
                    ShowToast.show(context, R.string.access_fail, true);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                /*Map<String, String> params = new HashMap<>();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            /*//添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(v1 >= 5) {
                //此为自定义的动画效果
                overridePendingTransition(R.anim.en, R.anim.ex);
            }*/
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) bitmap.recycle();
    }

}
