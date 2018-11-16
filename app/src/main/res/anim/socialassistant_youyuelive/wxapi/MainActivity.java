package com.socialassistant_youyuelive.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.socialassistant_youyuelive.AV.AVChatActivity;
import com.socialassistant_youyuelive.AV.AVchat;
import com.socialassistant_youyuelive.AV.DemoApplication;
import com.socialassistant_youyuelive.AV.DemoCache;
import com.socialassistant_youyuelive.AV.permission.MPermission;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.VoucherActivity;
import com.socialassistant_youyuelive.adapter.MyFragmentPagerAdapter;
import com.socialassistant_youyuelive.commomentity.ActionBarColorManager;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.UpdateManager;
import com.socialassistant_youyuelive.commomentity.UserData;
import com.socialassistant_youyuelive.fragments.ListFragmentFollow;
import com.socialassistant_youyuelive.fragments.ListFragmentMe;
import com.socialassistant_youyuelive.fragments.ListFragmentMessage;
import com.socialassistant_youyuelive.service.LocationService;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.OkHttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        ViewPager.OnPageChangeListener {

    public static final String TAG = "MainActivity";
    private static final int CONN_YUNXIN = 0x123;
    private static final int NEW_DATA = 666;
    private Context context;
    private String account;
    private String tokenId;
    public static boolean isFirstLogin = true;
                                                                                                                                                                                                                                                                                                             private static final int UPLOAD_JPUSH_ID = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_YUNXIN: {
                    Log.i(TAG, account + " handler " + tokenId);
                    // 注册网络通话来电
                    enableAVChat();
                    aVchat.login(account, tokenId, context);
                    break;
                }
                case UPLOAD_JPUSH_ID: {
                    //上传极光推送ID
                    uploadJPushID();
                    JPush_Alias();
                    break;
                }
                case NEW_DATA:
                    //解析登录过来的当前账号数据
                    parseUserData();
                    ConstString.updateUserData();
                    if (ConstString.isLogined && !TextUtils.isEmpty(ConstString.user)) LoginedParse(ConstString.user);
                    break;
            }
        }
    };

    private void uploadJPushID() {
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                ConstString.url_JPush_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (ConstString.isLiver) {
                    params.put("JP", ConstString.anchor_id);
                } else {
                    params.put("JP", ConstString.userId);
                }
                params.put("JPush", JPushInterface.getRegistrationID(getApplicationContext()));
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    public AVchat aVchat;
    //几个代表页面的常量
    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;
    public static final int PAGE_FOUR = 3;
    //百度定位SDK
    private LocationService locationService;
    //fragment
    public List<Fragment> mfragments;
    //UI 单选按钮组
    private RadioGroup radioGroup;
    private RadioButton btn_square,btn_message,btn_follow,btn_me;
    private ViewPager Vpager;
    private MyFragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBarColorManager.setColor(this, getResources().getColor(R.color.fense));
        context = this;
        ActivityCollector.addActivity(this);
        initView();
        // 6.0以上请求权限
        if (Build.VERSION.SDK_INT >= 23) requestBasicPermission(this);
        //创建数据表
        LitePal.getDatabase();
        //获取头部加密数据
        if (DemoApplication.FLAG) {
            getTokenAndKey();
            DemoApplication.FLAG = false;
        } else {
            //解析登录过来的当前账号数据
            parseUserData();
            ConstString.updateUserData();
            if (ConstString.isLogined && !TextUtils.isEmpty(ConstString.user)) LoginedParse(ConstString.user);
        }
        btn_square.setChecked(true);
        if (DemoApplication.isUpdate()) {
            GetNewVersionAPP();
            DemoApplication.setUpdate(false);
        }
    }

    private void LoginedParse(String user) {
        JSONObject jsonObject = JSON.parseObject(user);
        if (jsonObject == null) return;
        tokenId = jsonObject.getString("tokenId");
        String mobile = jsonObject.getString("mobile");
        if (TextUtils.isEmpty(mobile)) {
            //第三方可跳过进来的
            mobile = jsonObject.getString("openId");
        }
        ConstString.mobile = mobile;
        try {
            if (!DemoCache.getIsLoginYunxin()){
                handler.sendEmptyMessage(CONN_YUNXIN);
            }
        } catch (Exception e){
            Log.i(TAG, e.getMessage());
        }
        String data = "";
        data = jsonObject.getString("nickName");
        ConstString.nickName = (data != null && !data.trim().equals("")) ? data.trim() : "";
        //主播没有该字段，用户有
        ConstString.isLiver = !jsonObject.containsKey("uType");
        if (ConstString.isLiver) {
            data = jsonObject.getString("anchorId");
            ConstString.anchor_id = (data != null && !data.trim().equals("")) ? data.trim() : "";
            handler.sendEmptyMessage(UPLOAD_JPUSH_ID);
            DemoCache.setAnchorId(ConstString.anchor_id);
            DemoCache.setIsLiver(true);
            account = ConstString.anchor_id;
        } else {
            data = jsonObject.getString("userId");
            ConstString.userId = (data != null && !data.trim().equals("")) ? data.trim() : "";
            handler.sendEmptyMessage(UPLOAD_JPUSH_ID);
            DemoCache.setAccountId(ConstString.userId);
            DemoCache.setIsLiver(false);
            account = ConstString.userId;
        }
        if (jsonObject.containsKey("balance")){
            Log.i(TAG, "" + jsonObject.getLongValue("balance"));
            VoucherActivity.setBalance(jsonObject.getLongValue("balance"));
        }
        if (jsonObject.containsKey("headImgUrl")){
            DemoCache.setAccountHeadImg(jsonObject.getString("headImgUrl"));
        }else {
            DemoCache.setAccountHeadImg(null);
        }
        if (jsonObject.containsKey("nickName")){
            DemoCache.setAccountNickName(jsonObject.getString("nickName"));
        }else {
            DemoCache.setAccountNickName(null);
        }
    }

    private void JPush_Alias() {
        //我的个人思路是，当用户登录的时候，将用户名作为别名，调用如下代码进行设置即可：
        String name = ConstString.isLiver ? ConstString.anchor_id : ConstString.userId;
        String JPushID = JPushInterface.getRegistrationID(getApplicationContext());
        //ShowToast.normalShow(context, name + "==" + JPushID, true);
        JPushInterface.setAlias(context, name + JPushID,
                new TagAliasCallback() {

                    @Override
                    public void gotResult(int responseCode, String alias, Set<String> tags) {
                        /*if (responseCode==0) {
                            System.out.println("极光推送别名设置成功!");
                        }*/
                    }
                });
    }

    private void parseUserData() {
        Intent intent_Login = getIntent();
        if (intent_Login == null) return;
        String user = intent_Login.getStringExtra("user");
        if (TextUtils.isEmpty(user)) return;
        String token_ = intent_Login.getStringExtra("token");
        String key_ = intent_Login.getStringExtra("key");
        //判断是否登录唯一标志
        ConstString.isLogined = true;
        //intent_Login.getBooleanExtra("isLogined", false);
        //当前账号所有数据，需要可以在这里里面进行解析出想要的数据
        ConstString.user = user;
        JSONObject jsonObject = JSON.parseObject(user);
        tokenId = jsonObject.getString("tokenId");
        String mobile = jsonObject.getString("mobile");
        if (TextUtils.isEmpty(mobile)) {
            //第三方可跳过进来的
            mobile = jsonObject.getString("openId");
        }
        ConstString.mobile = mobile;
        //主播没有该字段，用户有
        ConstString.isLiver = !jsonObject.containsKey("uType");
        //保存数据到数据库
        //删除，每次只保存一条登录数据
        DataSupport.deleteAll(UserData.class);
        UserData userData = new UserData();
        userData.setUserData(user);
        userData.setLiver(ConstString.isLiver);
        userData.setLogined(ConstString.isLogined);
        userData.setToken(token_);
        userData.setKey(key_);
        userData.setMobile(ConstString.mobile);
        userData.save();
        Log.i(TAG, tokenId + "");
        Log.i(TAG, account + "");
        try {
            if (!DemoCache.getIsLoginYunxin()){
                handler.sendEmptyMessage(CONN_YUNXIN);
            }
        } catch (Exception e){
            Log.i(TAG, e.getMessage());
        }
        String data = "";
        data = jsonObject.getString("nickName");
        ConstString.nickName = (data != null && !data.trim().equals("")) ? data.trim() : "";
        if (ConstString.isLiver) {
            data = jsonObject.getString("anchorId");
            ConstString.anchor_id = (data != null && !data.trim().equals("")) ? data.trim() : "";
            DemoCache.setAnchorId(ConstString.anchor_id);
            DemoCache.setIsLiver(true);
            account = ConstString.anchor_id;
        } else {
            data = jsonObject.getString("userId");
            ConstString.userId = (data != null && !data.trim().equals("")) ? data.trim() : "";
            DemoCache.setAccountId(ConstString.userId);
            DemoCache.setIsLiver(false);
            account = ConstString.userId;
        }
        if (jsonObject.containsKey("balance")){
            Log.i(TAG, "" + jsonObject.getLongValue("balance"));
            VoucherActivity.setBalance(jsonObject.getLongValue("balance"));
        }
        if (jsonObject.containsKey("headImgUrl")){
            DemoCache.setAccountHeadImg(jsonObject.getString("headImgUrl"));
        }else {
            DemoCache.setAccountHeadImg(null);
        }
        if (jsonObject.containsKey("nickName")){
            DemoCache.setAccountNickName(jsonObject.getString("nickName"));
        }else {
            DemoCache.setAccountNickName(null);
        }
    }

    @Override
    protected void onStop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        ConstString.lng = null;
        ConstString.lat = null;
        if(locationService != null){
            locationService.unregisterListener(mListener); //注销掉监听
            locationService.stop(); //停止定位服务
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //在程序开始执行的时候 就开始定位
        locationService = ((DemoApplication)getApplication()).locationService;
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();
    }

    /*****
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDLocationListener mListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                //因为第三方的地图获取坐标的时候会出现误差,所以要进行转换
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(new LatLng(location.getLongitude(),location.getLatitude()));
                LatLng baiduLatLng = converter.convert();
//                //经度
//                ConstString.lng = String.valueOf(location.getLongitude() - 0.008774687519);
//                //纬度
//                ConstString.lat = String.valueOf(location.getLatitude() - 0.00374531687912);
                //经度
                ConstString.lng = String.valueOf(baiduLatLng.latitude - 0.008774687519);
                //纬度
                ConstString.lat = String.valueOf(baiduLatLng.longitude - 0.00374531687912);
                //只会走一次,下次不会再走
                if(isFirstLogin){
                    ConstString.updateUserData();
                    if(ConstString.isLiver){
                        if(!TextUtils.isEmpty(ConstString.anchor_id)){
                            updateUserOrAnchorLocationMsg(ConstString.anchor_id,ConstString.lng,ConstString.lat);
                        }
                    }else {
                        if(!TextUtils.isEmpty(ConstString.userId)){
                            updateUserOrAnchorLocationMsg(ConstString.userId,ConstString.lng,ConstString.lat);
                        }
                    }
                }
            }
        }
        public void onConnectHotSpotMessage(String s, int i){
        }
    };
    public void updateUserOrAnchorLocationMsg(String userIdoranchorId,String lng,String lat){
        Map<String,String> params = new HashMap<>();
        params.put("userId",userIdoranchorId);
        params.put("lng",lng);
        params.put("lat",lat);
        HttpUtil.uploadUserOrAnchorLocationMsg(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(!TextUtils.isEmpty(s)){
                    isFirstLogin = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
        },params);
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ShowToast.show(context, R.string.exit_app, true);
                exitTime = System.currentTimeMillis();
            } else {
                /*ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.clearMemoryCache();
                imageLoader.clearDiskCache();*/
                NIMClient.getService(AuthService.class).logout();
                DemoCache.clear();
                ActivityCollector.finishAll();
                ConstString.updateUserData();
                if (ConstString.isLiver){
                    // 更新主播状态(离线)
                    final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
                    scheduExec.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 1.主播Id,2.状态 0忙碌中 1可通话 2是免打扰
                                Map<String, Object> status = new HashMap<String, Object>();
                                status.put("anthorId", DemoCache.getAnchorId());
                                status.put("type", 3);
                                String result = OkHttpUtil.doPost(OkHttpUtil.UPDATE_STATUS, status);
                                if (!result.equals("")){
                                    // 解析返回的数据并打印
                                    JSONObject json = JSONObject.parseObject(result);
                                    boolean isSendSuccess = json.getBoolean("status");
                                    if (isSendSuccess){
                                        //        Intent intent = new Intent(activity, HeartBeatService.class);
                                        //        activity.stopService(intent);
                                        Process.killProcess(Process.myPid());
                                        System.exit(0);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, 3, TimeUnit.SECONDS);
                }else {
                    //        Intent intent = new Intent(activity, HeartBeatService.class);
                    //        activity.stopService(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void requestBasicPermission(Activity activity) {
        MPermission.printMPermissionResult(true, activity, BASIC_PERMISSIONS);
        MPermission.with(activity)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    private final int BASIC_PERMISSION_REQUEST_CODE = 100;

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.RECORD_AUDIO,
    };

    /**
     * 音视频通话配置与监听
     */
    private void enableAVChat() {
        registerAVChatIncomingCallObserver(true);
    }

    // 监听来电
    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                String extra = data.getExtra();
                Log.i(TAG, "Extra Message->" + extra);
                if (AVchat.getIsAvChating()
                        || AVChatManager.getInstance().getCurrentChatId() != 0) {
                    Log.i(TAG, "reject incoming call data =" + data.toString() + " as local phone is not idle");
                    AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);
                    return;
                }
//                // 将扩展内容转换为json对象
//                JSONObject extraJson = null;
//                try {
//                    extraJson = JSONObject.parseObject(extra);
//                }catch (Exception e){
//                    extraJson = null;
//                }
//                // 初始化头像为null
//                DemoCache.setShowHeadImgUri(null);
//                if (extraJson != null){
//                    if (extraJson.containsKey("headImg")){
//                        DemoCache.setShowHeadImgUri(extraJson.getString("headImg"));
//                    }
//                }
                if (AVChatActivity.needFinish){
                    // 需要加载视频聊天的activity
                    // AVChatData位来自云信的请求账号名称
                    aVchat.launch(ActivityCollector.getCurrentActivity(), data, AVChatActivity.FROM_BROADCASTRECEIVER);
                }
            }
        }, register);
    }

    ///////////////////////
    private void initView() {
        mfragments = new ArrayList<>();
        mfragments.add(new ListFragmentSquare());
        mfragments.add(new ListFragmentMessage());
        mfragments.add(new ListFragmentFollow());
        mfragments.add(new ListFragmentMe());
        radioGroup = (RadioGroup) findViewById(R.id.main_group_bottom);
        btn_square = (RadioButton) findViewById(R.id.main_radio_item);
        btn_message = (RadioButton) findViewById(R.id.main_radio_message);
        btn_follow = (RadioButton) findViewById(R.id.main_radio_follow);
        btn_me = (RadioButton) findViewById(R.id.main_radio_me);
        //radpoint = (LinearLayout) findViewById(R.id.main_rad_point);
        radioGroup.setOnCheckedChangeListener(this);
        Vpager = (ViewPager) findViewById(R.id.main_Vpager);
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),mfragments);
        Vpager.setAdapter(mAdapter);
        Vpager.setCurrentItem(0);
        Vpager.addOnPageChangeListener(this);
        aVchat = new AVchat();
    }

    //重写ViewPager页面切换的处理方法
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
        if(state == 2){
            switch (Vpager.getCurrentItem()){
                case PAGE_ONE:btn_square.setChecked(true);break;
                case PAGE_TWO:btn_message.setChecked(true);break;
                case PAGE_THREE:btn_follow.setChecked(true);break;
                case PAGE_FOUR:{
                    //ListFragmentMe fragmentMe = (ListFragmentMe)mfragments.get(PAGE_FOUR);
                    //fragmentMe.updateMeInfo();
                    btn_me.setChecked(true);
                    break;
                }
            }
        }
    }

    //处理RadioGroup监听方法
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.main_radio_item:{
                Vpager.setCurrentItem(PAGE_ONE);
                break;
            }
            case R.id.main_radio_message:{
                if (!ConstString.isLogined) {
                    btn_message.setChecked(false);
                    btn_square.setChecked(true);
                    startActivity(new Intent(MainActivity.this, WXEntryActivity.class));
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                    finish();
                    return;
                }
                //radpoint.setVisibility(View.INVISIBLE);
                Vpager.setCurrentItem(PAGE_TWO);
                break;
            }
            case R.id.main_radio_follow:{
                if (!ConstString.isLogined) {
                    btn_message.setChecked(false);
                    btn_square.setChecked(true);
                    startActivity(new Intent(MainActivity.this, WXEntryActivity.class));
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                    finish();
                    return;
                }
                Vpager.setCurrentItem(PAGE_THREE);
                break;
            }
            case R.id.main_radio_me:{
                if (!ConstString.isLogined) {
                    btn_message.setChecked(false);
                    btn_square.setChecked(true);
                    startActivity(new Intent(MainActivity.this, WXEntryActivity.class));
                    //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                    finish();
                    return;
                }
                Vpager.setCurrentItem(PAGE_FOUR);
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //界面挂起就进行垃圾回收
        System.gc();
    }

    private Map<String, String> params = new HashMap<>();
    private void getNewData(String userId) {
        params.put("userId", userId);
        params.put("time", String.valueOf(System.currentTimeMillis()));
        final String sign = HttpUtil.createSign(params, ConstString.API_KEY);
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                ConstString.URL_KEY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) return;
                        String state = jsonObject.getString("state");
                        if (state != null && state.equals("ok")) {
                            ConstString.user = jsonObject.getString("user");
                            UserData userData = DataSupport.findFirst(UserData.class);
                            if (userData != null) {
                                userData.setUserData(ConstString.user);
                                userData.save();
                                handler.sendEmptyMessage(NEW_DATA);
                                DemoApplication.FLAG = false;
                            }
                        } else {
                            //清空数据库
                            DataSupport.deleteAll(UserData.class);
                            Intent intent = new Intent(context, WXEntryActivity.class);
                            startActivity(intent);
                            ShowToast.normalShow(context, "为了保护账号安全,我们定期更改了秘钥!\n" +
                                    "请重新登录进来!", true);
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (error instanceof NetworkError) {
                    ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                } else if (error instanceof ServerError) {
                    ShowToast.normalShow(context, "服务器异常，请稍后重试!", true);
                } else if (error instanceof AuthFailureError) {
                    ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                    //清空数据库
                    DataSupport.deleteAll(UserData.class);
                    Intent intent = new Intent(context, WXEntryActivity.class);
                    startActivity(intent);
                    finish();
                    ShowToast.normalShow(context, "为了保护账号安全,我们定期更改了秘钥!\n" +
                            "请重新登录进来!", true);
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> pa = new HashMap<>();
                pa.put("sign", sign);
                pa.put("key", ConstString.KEY);
                return pa;
            }
        };
        mQueue.add(postRequest);
    }

    private void GetNewVersionAPP() {
        final String versionName = UpdateManager.getVersion(context);
        if (TextUtils.isEmpty(versionName)) return;
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                ConstString.NEW_VERSION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject == null) return;
                        String state = jsonObject.getString("status");
                        if (state != null && state.equals("true")) {
                            String value = jsonObject.getString("values");
                            if (TextUtils.isEmpty(value)) return;
                            ConstString.APK_URL = value;
                            //弹出更新提醒对话框
                            showUpdataDialog();
                            DemoApplication.setUpdate(false);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("versionCode", String.valueOf(versionName));
                return params;
            }

        };
        mQueue.add(postRequest);
    }

    private void showUpdataDialog() {
        new AlertDialog.Builder(context)
                .setTitle("更新提醒")
                .setMessage("1.更新了许多界面,界面更加人性化!\n" +
                        "2.支付更加安全,主播提现更加方便!\n" +
                        "3.软件目前发现一些bug做了清除!")
                .setCancelable(true)
                .setPositiveButton("马上更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 显示下载对话框
                        UpdateManager manager = new UpdateManager(context);
                        manager.showDownloadDialog();
                    }
                }).show();
    }

    private void getTokenAndKey() {
        //获取token app_key 个人数据
        ConstString.updateUserData();
        if (ConstString.isLogined) {
            JSONObject jsonObject = JSON.parseObject(ConstString.user);
            if (jsonObject != null) {
                String data = "";
                if (ConstString.isLiver) {
                    data = jsonObject.getString("anchorId");
                    ConstString.anchor_id = !TextUtils.isEmpty(data) ? data.trim() : "";
                    //请求最新的token app_key 个人数据
                    getNewData(ConstString.anchor_id);
                } else {
                    data = jsonObject.getString("userId");
                    ConstString.userId = !TextUtils.isEmpty(data) ? data.trim() : "";
                    //请求最新的token app_key 个人数据
                    getNewData(ConstString.userId);
                }
            }
        }
    }

}
