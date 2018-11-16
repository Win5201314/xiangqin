package com.socialassistant_youyuelive.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jerey.animationlib.AnimationHelper;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.service.UploadService;
import com.socialassistant_youyuelive.util.ShowToast;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;
import com.yanzhenjie.album.Album;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BecomeAnchorActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.head) ImageView head;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.image_ok) ImageView image_ok;
    @BindView(R.id.vedio) ImageView vedio;
    @BindView(R.id.vedio_ok) ImageView vedio_ok;
    @BindView(R.id.voice) ImageView voice;
    @BindView(R.id.voice_ok) ImageView voice_ok;
    @BindView(R.id.upload) Button upload;
    @BindView(R.id.show) TextView show;

    private static String[] pathImages = new String[4];
    private static String pathVedio1, pathAudio;
    private static boolean vedio1 = false;
    private static final int NEW_VEDIO = 133;
    private Context context;

    private void showSuccessDialog() {
        new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle("审核资料上传成功")
                .setMessage(R.string.upload_success_oss)
                .setCancelable(true)
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_anchor);
        initToolbar(R.id.toolbar, R.id.title, "主播认证");
        context = this;
        ButterKnife.bind(this);
        head.setOnClickListener(this);
        image.setOnClickListener(this);
        vedio.setOnClickListener(this);
        voice.setOnClickListener(this);
        upload.setOnClickListener(this);
        if (!isConn(context)) {
            ShowToast.normalShow(context, "请检查网络，网络异常！", true);
            ToSetting();
        }
        requestPermissionREAD_WRITEAndroid_6();
        Alerter.create(BecomeAnchorActivity.this)
                .setTitle("温馨提示")
                .setText("下面的5张照片第一张将作为背景图，\n成为主播之后5张都将不能更改了\n请认真选择图片...")
                .setBackgroundColorRes(R.color.colorAccent) // or setBackgroundColorInt(Color.CYAN)
                .enableSwipeToDismiss()
                .setDuration(7000)
                .show();
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
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
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /*if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //已经禁止提示了
                ShowToast.show(context, R.string.NO_PERMISSION, true);
            } else {
                ActivityCompat.requestPermissions(BecomeAnchorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }*/
            ActivityCompat.requestPermissions(BecomeAnchorActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
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

    /*
     * 判断网络连接是否已开
     * 2012-08-20
     *true 已打开  false 未打开
     * */
    public static boolean isConn(Context context){
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if (network != null) return conManager.getActiveNetworkInfo().isAvailable();
        return false;
    }

    private void ToSetting() {
        Intent intent = null;
        //判断手机系统的版本  即API大于10 就是3.0或以上版本
        if(android.os.Build.VERSION.SDK_INT>10){
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        }else{
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        context.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.head:getAlbum("请选择1张图片作为头像", 1, 1000);break;
            case R.id.image:getAlbum("请选择3张图片", 3, 999);break;
            case R.id.vedio:GET_Vedio();break;
            case R.id.voice:
                    AnimationHelper.startActivityForResult(BecomeAnchorActivity.this,
                            new Intent(BecomeAnchorActivity.this, VoiceRecorderActivity.class),
                            222, findViewById(R.id.voice),
                            R.color.search_bg);
                break;
            case R.id.upload: {
                if (isFull(pathImages) && vedio1 && /*!TextUtils.isEmpty(pathAudio) &&*/ !TextUtils.isEmpty(pathVedio1)) {
                    //show.setVisibility(View.VISIBLE);
                    ConstString.updateUserData();
                    if (!TextUtils.isEmpty(ConstString.mobile)) {
                        //启动一个后台服务上传数据到阿里云
                        Intent intent = new Intent(BecomeAnchorActivity.this, UploadService.class);
                        intent.putExtra("face", pathImages[0]);
                        intent.putExtra("album_1", pathImages[1]);
                        intent.putExtra("album_2", pathImages[2]);
                        intent.putExtra("album_3", pathImages[3]);
                        //intent.putExtra("album_4", pathImages[4]);
                        //intent.putExtra("album_5", pathImages[5]);
                        intent.putExtra("vedio", pathVedio1);
                        //intent.putExtra("voice", pathAudio);
                        startService(intent);
                        showSuccessDialog();
                    } else {
                        ShowToast.normalShow(context, "账号丢失!请重新登录进来!", true);
                    }
                } else {
                    ShowToast.normalShow(context, "请将上面信息补充完整！\n才能上传审核!", true);
                }
                break;
            }
        }
    }

    private boolean isFull(String[] pathImages) {
        for (String s: pathImages) if (TextUtils.isEmpty(s)) return false;
        return true;
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
            ActivityCompat.requestPermissions(BecomeAnchorActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            ArrayList<String> mImageList = new ArrayList<>();
            Album.album(BecomeAnchorActivity.this)
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
    private Uri fileUri;
    private int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 120;
    private static File vedioFile;
    private void GET_Vedio() {
        /*获取当前系统的android版本号*/
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24){
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                vedioFile = createMediaFile();
                fileUri = Uri.fromFile(vedioFile); // create a file to save the video
            } catch (IOException e) {
                e.printStackTrace();
            }
            //intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 8);
            //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.5); // set the video image quality to high
            /*MediaStore.EXTRA_VIDEO_QUALITY- 此值在最低质量最小文件尺寸时是0，在最高质量最大文件尺寸时是１.
                MediaStore.EXTRA_DURATION_LIMIT- 此值设置获取视频的长度，以秒为单位．
                MediaStore.EXTRA_SIZE_LIMIT- 此值设置获取视频文件的大小，以字节为单位．*/
                /*intent.putExtra("camerasensortype", 2); // 调用前置摄像头
            intent.putExtra("autofocus", true); // 自动对焦
            intent.putExtra("fullScreen", false); // 全屏*/
            //intent.putExtra("showActionIcons", false);
            // start the Video Capture Intent
            startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        } else {
            //安卓7.0开始
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            try {
                vedioFile = createMediaFile();
                if (vedioFile.exists()) {
                    fileUri = Uri.fromFile(vedioFile); // create a file to save the video
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, vedioFile.getAbsolutePath());
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 8);
            startActivityForResult(intent, NEW_VEDIO);
        }
    }

    private File createMediaFile() throws IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES), "CameraDemo");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    //Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "VID_" + timeStamp;
            String suffix = ".mp4";
            File mediaFile = new File(mediaStorageDir + File.separator + imageFileName + suffix);
            return mediaFile;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //相册
        if(requestCode == 999) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                ArrayList<String> imageList = Album.parseResult(data);
                if (imageList.size() < 3) {
                    ShowToast.normalShow(context, "必选3张!", true);
                    image_ok.setVisibility(View.GONE);
                    return;
                }
                //判断图片大小
                for (int i = 0; i < imageList.size(); i++) {
                    String path = imageList.get(i);
                    if (TextUtils.isEmpty(path)) continue;
                    pathImages[i + 1] = path;
                    switch (i) {
                        case 0:
                            //使用最少的内存完成复杂的图片转换，转换图片以适合所显示的ImageView，来减少内存消耗
                            //加载本地文件
                            Picasso.with(context).load(new File(path))
                                    //裁剪图片尺寸
                                    .resize(50, 50)
                                    //设置图片圆角
                                    .centerCrop()
                                    .into(image);
                            image_ok.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
        //头像
        if(requestCode == 1000) {
            if (resultCode == RESULT_OK) { // Successfully.
                // Parse select result.
                ArrayList<String> imageList = Album.parseResult(data);
                String facePath = imageList.get(0);
                if (!TextUtils.isEmpty(facePath)) {
                    File file = new File(facePath);
                    pathImages[0] = facePath;
                    //使用最少的内存完成复杂的图片转换，转换图片以适合所显示的ImageView，来减少内存消耗
                    Picasso.with(context).load(file)
                            //裁剪图片尺寸
                            .resize(50, 50)
                            //设置图片圆角
                            .centerCrop()
                            .into(head);
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled.
            }
        }
        //视频1
        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            try {
                String path = vedioFile.getAbsolutePath();
                File file = new File(path);
                if (!file.exists()) {
                    ShowToast.show(context, R.string.file_no_exist, true);
                    return;
                }
                if (file.length() > 30 * 1024 * 1024) {
                    ShowToast.show(context, R.string.file_too_big, true);
                    return;
                }
                pathVedio1 = path;
                getFirstVedioImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //语音
        if (requestCode == 222) {
            switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
                case RESULT_OK:
                    Bundle b = data.getExtras(); //data为B中回传的Intent
                    if (b != null) {
                        //录音文件绝对路径
                        try {
                            String path = b.getString("path");//str即为回传的值
                            File file = new File(path);
                            if (!file.exists()) {
                                ShowToast.show(context, R.string.file_no_exist, true);
                                return;
                            } else {
                                if (file.length() > 5 * 1024 * 1024) {
                                    ShowToast.show(context, R.string.audio_too_big, true);
                                    return;
                                }
                                if (file.length() <= 0) {
                                    ShowToast.show(context, R.string.file_no_size, true);
                                    return;
                                }
                                pathAudio = path;
                                voice_ok.setVisibility(View.VISIBLE);
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        //视频2
        if (requestCode == NEW_VEDIO) {
            if (data == null) return;
            try {
                String path = vedioFile.getAbsolutePath();
                File file = new File(path);
                if (!file.exists()) {
                    ShowToast.show(context, R.string.file_no_exist, true);
                    return;
                }
                if (file.length() > 30 * 1024 * 1024) {
                    ShowToast.show(context, R.string.file_too_big, true);
                    return;
                }
                pathVedio1 = path;
                getFirstVedioImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getFirstVedioImage(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        Bitmap bitmap = media.getFrameAtTime();
        if (bitmap != null) {
            vedio.setImageBitmap(bitmap);
            vedio1 = true;
            vedio_ok.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Save Bitmap to a file.保存图片到SD卡。
     *
     * @param bitmap
     * @return error message if the saving is failed. null if the saving is
     *         successful.
     * @throws IOException
     */
    public static void saveBitmapToFile(Bitmap bitmap, String _file) throws IOException {
        //_file = getSDPath()+"/xx自定义文件夹/hot.png"
        BufferedOutputStream os = null;
        try {
            File file = new File(_file);
            int end = _file.lastIndexOf(File.separator);
            String _filePath = _file.substring(0, end);
            File filePath = new File(_filePath);
            if (!filePath.exists()) filePath.mkdirs();
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取SD卡路径
     * @return
     */
    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        //判断sd卡是否存在
        if   (sdCardExist) {
            //获取跟目录
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir.toString();
    }

}
