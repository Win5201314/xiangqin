package com.socialassistant_youyuelive.AV;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.service.LocationService;
import com.socialassistant_youyuelive.wxapi.MainActivity;
import com.tencent.bugly.crashreport.CrashReport;

import org.litepal.LitePal;

import java.io.File;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zjm on 2017/4/21.
 */

public class DemoApplication extends Application {
    //Volley 初始化队列
    private static RequestQueue queue;
    //慎用
    public static  DemoApplication context;
    private static final String TAG = "DemoApplication";
    //初始化百度SDK
    public LocationService locationService;
    public Vibrator mVibrator;

    private static Context mcontext;
    //获取token app_key 个人数据， 唯一标志, 唯一使用，不要随便动
    public static boolean FLAG = true;
    //更新软件
    private static boolean isUpdate = true;

    public static boolean isUpdate() {
        return isUpdate;
    }

    public static void setUpdate(boolean update) {
        isUpdate = update;
    }

    //wifi提醒对话框是否不在提示
    public static boolean isStop = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = getApplicationContext();
        LitePal.initialize(mcontext);
        JPushInterface.setDebugMode(false); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        context = this;
        queue = Volley.newRequestQueue(context);
        initImageLoader();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        CrashReport.initCrashReport(getApplicationContext(), "f748d0e2aa", true);
        try{
            SDKInitializer.initialize(getApplicationContext());
            Log.d("百度定位已启用","----------------------------------------1");
        }catch (Exception e){
            e.printStackTrace();
        }
        // ... your codes
        DemoCache.setContext(this);

        // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）
        NIMClient.init(this, loginInfo(), options());

//        // ... your codes
//        if (inMainProcess()) {
//            // 注意：以下操作必须在主进程中进行
//            // 1、UI相关初始化操作
//            // 2、相关Service调用
//        }
        //获取广告地址
        getBannerUrl();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public void getBannerUrl() {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(
                Request.Method.GET,
                ConstString.Banner_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //{"status":true,
                        // "message":"获取成功",
                        // "values":["http://m5.seorj.cn:80/video/images/1.png",
                        // "http://m5.seorj.cn:80/video/images/2.png",
                        // "http://m5.seorj.cn:80/video/images/3.png"]}
                        //ShowToast.normalShow(context, response, true);
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) return;
                        String values = jsonObject.getString("values");
                        if (!TextUtils.isEmpty(values)) {
                            JSONArray array = JSON.parseArray(values);
                            if (array != null && array.size() > 0) {
                                String s = "";
                                for (int i = 0; i < array.size(); i++) {
                                    if (array.get(i) != null) s += array.get(i).toString() + ",";
                                }
                                SharedPreferences sharedPreferences = getSharedPreferences("banner", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("banner", s);
                                editor.commit();
                                //ShowToast.normalShow(context, s, true);
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
        mQueue.add(postRequest);
    }

    public static RequestQueue getQueue(){
        return queue;
    }
    //初始化imageloader参数
    public void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(this, ConstString.PATH_CACHE);
        ImageLoaderConfiguration configuration= new ImageLoaderConfiguration
                .Builder(this)
                .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY -2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2* 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(5 * 1024 * 1024)
                .discCacheSize(50 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .discCacheFileCount(2000) //缓存的文件数量
                .discCache(new UnlimitedDiskCache(cacheDir))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(this,5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for releaseapp
                .build();//开始构建
        ImageLoader.getInstance().init(configuration);
    }


    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MainActivity.class; // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.mipmap.ic_launcher;
        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.netease.nim.demo/raw/msg";
        options.statusBarNotificationConfig = config;

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用下面代码示例中的位置作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = 480 / 2;

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

            @Override
            public int getDefaultIconResId() {
                return R.mipmap.ic_launcher;
            }

            @Override
            public Bitmap getTeamIcon(String tid) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(String account) {
                return null;
            }

            @Override
            public String getDisplayNameForMessageNotifier(String s, String s1, SessionTypeEnum sessionTypeEnum) {
                return null;
            }

        };
        return options;
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        return null;
    }

}
