package com.socialassistant_youyuelive.wxapi;

import android.Manifest;
import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.PhoneLoginActivity;
import com.socialassistant_youyuelive.activity.ResgitActivity;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.EmojiFilter;
import com.socialassistant_youyuelive.commomentity.UserInfo;
import com.socialassistant_youyuelive.util.ShowToast;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.socialassistant_youyuelive.IPackageDataObserver;
import com.socialassistant_youyuelive.IPackageStatsObserver;

import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler, OnClickListener {
    private PackageManager pm;
    StringBuilder sb = new StringBuilder();
    StringBuilder sbCache = new StringBuilder();
    private long cacheS;
    Handler mHadler = new Handler();
    /////////////////////////////////////////////////////
    // 微信开放平台申请到的app_id
    public static final String APP_ID = ConstString.APP_ID;
    // 微信开放平台申请到的app_id对应的app_secret
    public static final String APP_SECRET = ConstString.APP_SECRET;
    // 用于请求用户信息的作用域
    private static final String WEIXIN_SCOPE = "snsapi_userinfo";
    // 自定义
    private static final String WEIXIN_STATE = "login_state_001";
    //IWXAPI是第三方APP与微信通信的接口
    private IWXAPI api;
    private UserInfo userInfo;
    private ShimmerTextView tv1, tv2;
    private TextView WXLogin, phoneLogin;
    private ImageView WXLogin2;
    static final int RETURN_OPENID_ACCESSTOKEN = 0;// 返回openid，accessToken消息码
    static final int RETURN_OPENID = 1; // 返回openid
    static final int RETURN_To_Login = 2; // 是否注册，去哪里
    private static final String url_openid = ConstString.IP + "/video/user/userLogin";
    private ProgressDialog progressDialog1;
    private static final int DIMISSION = 3;//关闭对话框
    private Context context;
    private Shimmer shimmer = new Shimmer();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RETURN_OPENID_ACCESSTOKEN:
                    Bundle bundle1 = (Bundle) msg.obj;
                    getUserinfo(bundle1.getString("openid").trim(), bundle1.getString("access_token").trim());
                    break;
                case RETURN_OPENID:
                    Bundle bundle2 = (Bundle) msg.obj;
                    //检测后台是否保存了该用户注册数据
                    isExistOpenId(bundle2.getString("openid").trim());
                    break;
                case RETURN_To_Login:
                    Bundle bundle3 = (Bundle) msg.obj;
                    String isExist = bundle3.getString("isExist");
                    handler.sendEmptyMessage(DIMISSION);
                    if (!TextUtils.isEmpty(isExist) && isExist.equals("ok")) {
                        //存在注册信息
                        String user = bundle3.getString("user");
                        Intent intent = new Intent();
                        intent.setAction(ConstString.MAIN_ACTION);
                        intent.putExtra("user", user);
                        intent.putExtra("isLogined", true);
                        intent.putExtra("type", bundle3.getInt("type", -1));
                        ConstString.isLogined = true;
                        intent.putExtra("JPush", true);
                        String token = bundle3.getString("token");
                        String key = bundle3.getString("key");
                        intent.putExtra("token", token);
                        intent.putExtra("key", key);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                        finish();
                    } else {
                        //不存在注册信息，去注册绑定
                        Intent intent = new Intent();
                        intent.setClass(WXEntryActivity.this, ResgitActivity.class);
                        Bundle bundle = new Bundle();
                        userInfo.setRegistrationWay("0");
                        bundle.putSerializable("userinfo", userInfo);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                        finish();
                    }
                    break;
                case DIMISSION: {
                    if (progressDialog1 != null) progressDialog1.dismiss();
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog1 != null) progressDialog1.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_show);
        ActivityCollector.addActivity(this);
        context = this;
        // 注册到微信列表
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);
        //将你收到的intent和实现IWXAPIEventHandler接口的对象传递给handleIntent方法
        api.handleIntent(getIntent(), this);

        WXLogin = (TextView) findViewById(R.id.WXLogin);
        WXLogin.setOnClickListener(this);
        WXLogin2 = (ImageView) findViewById(R.id.WXLogin2);
        WXLogin2.setOnClickListener(this);
        phoneLogin = (TextView) findViewById(R.id.phoneLogin);
        phoneLogin.setOnClickListener(this);
        //微光效果
        tv1 = (ShimmerTextView) findViewById(R.id.tv1);
        tv2 = (ShimmerTextView) findViewById(R.id.tv2);
        shimmer/*.setRepeatCount(0)*/
                .setDuration(1300)
                .setStartDelay(400)
                .setDirection(Shimmer.ANIMATION_DIRECTION_LTR);
        shimmer.start(tv1);
        shimmer.start(tv2);

        requestPermissionAndroid_6();
    }

    private void requestPermissionAndroid_6() {
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WXEntryActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            //微信登录
            case  R.id.WXLogin2:
            case R.id.WXLogin:
                /*Log.d("logger", api.isWXAppSupportAPI() + "========"
                        + api.getWXAppSupportAPI() + "-------------"
                        + api.isWXAppInstalled());*/
                if (isInstallWX()) {
                    /*getCaches();
                    cleanAll();*/
                    //Toast.makeText(this, getResources().getString(R.string.wx_loging), Toast.LENGTH_LONG).show();
                    progressDialog1 = new ProgressDialog(context);
                    progressDialog1.setTitle(getResources().getString(R.string.WX_login));
                    progressDialog1.setMessage(getResources().getString(R.string.wx_loging));
                    progressDialog1.setCancelable(true);
                    progressDialog1.show();
                    sendAuth();
                } else {
                    ShowToast.show(context, R.string.wechat_not_installed, true);
                }
                break;
            //手机注册/登录
            case R.id.phoneLogin:
                startActivity(new Intent(this, PhoneLoginActivity.class));
                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                if(version  >= 5) {
                    //此为自定义的动画效果，下面两个为系统的动画效果
                    //overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                }
                finish();
                break;
        }
    }

    //检测是否安装了微信软件
    private boolean isInstallWX() {
        // 获取手机已安装的所有应用package的信息(其中包括用户自己安装的，还有系统自带的)
        for (PackageInfo pack : getPackageManager().getInstalledPackages(0))
            if (pack.packageName.equals("com.tencent.mm")) return true;
        return false;
    }

    //申请授权
    private void sendAuth() {
        if (api == null) api = WXAPIFactory.createWXAPI(context, APP_ID, true);
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = WEIXIN_SCOPE;
        req.state = WEIXIN_STATE;
        api.sendReq(req);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        //finish();
    }

    //请求回调接口
    @Override
    public void onReq(BaseReq req) {
        // TODO Auto-generated method stub
    }

    /*private static final String NAME = "WX_LOGIN";
    private static final String KEY = "WX_CODE";*/

    //请求响应回调接口
    @Override
    public void onResp(BaseResp resp) {
        String result = "";
        //int errorCode = resp.errCode;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {
                    result = getString(R.string.errcode_success);
                    ShowToast.normalShow(context, result, true);
                    // 用于分享时不要有这个，不能强转
                    SendAuth.Resp aures = (SendAuth.Resp) resp;
                    String code = aures.code;
                    /*SharedPreferences WxSp = getSharedPreferences(NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor WxSpEditor = WxSp.edit();
                    WxSpEditor.putString(KEY,code);
                    WxSpEditor.apply();*/
                    //微信授权返回结果，处理授权后操作,获取微信token
                    getAccessToken(code);
                    return;
                } else {
                    result = getString(R.string.share_success);
                    ShowToast.normalShow(context, result, true);
                    Intent intent = new Intent();
                    intent.setAction(ConstString.MAIN_ACTION);
                    startActivity(intent);
                    //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                    int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                    if(version  >= 5) {
                        //此为自定义的动画效果
                        overridePendingTransition(R.anim.sq_en, R.anim.ex);
                    }
                    finish();
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL: {
                    if (resp instanceof SendAuth.Resp) {
                        result = getString(R.string.errcode_cancel);
                    } else {
                        result = getString(R.string.share_fail);
                    }
                    ShowToast.normalShow(context, result, true);
                    Intent intent = new Intent();
                    intent.setAction(ConstString.MAIN_ACTION);
                    startActivity(intent);
                    //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                    int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                    if(version  >= 5) {
                        //此为自定义的动画效果
                        overridePendingTransition(R.anim.sq_en, R.anim.ex);
                    }
                    finish();
                }
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED: {
                if (resp instanceof SendAuth.Resp) {
                    result = getResources().getString(R.string.errcode_deny);
                } else {
                    result = getString(R.string.share_fail);
                }
                ShowToast.normalShow(context, result, true);
                Intent intent = new Intent();
                intent.setAction(ConstString.MAIN_ACTION);
                startActivity(intent);
                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                if(version  >= 5) {
                    //此为自定义的动画效果
                    overridePendingTransition(R.anim.sq_en, R.anim.ex);
                }
                finish();
                }
                break;
            case BaseResp.ErrCode.ERR_BAN:
                //ShowToast.normalShow(context, "------ERR_BAN------", true);
                //finish();
                break;
            default:{
                result = getResources().getString(R.string.errcode_unknown);
                ShowToast.normalShow(context, result, true);
                Intent intent = new Intent();
                intent.setAction(ConstString.MAIN_ACTION);
                startActivity(intent);
                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int version = Integer.valueOf(android.os.Build.VERSION.SDK);
                if(version  >= 5) {
                    //此为自定义的动画效果
                    overridePendingTransition(R.anim.sq_en, R.anim.ex);
                }
                finish();
                }
                break;
        }
    }

    //获取第一步的code后，请求以下链接获取access_token
    //获取openid accessToken值用于后期操作,code请求码
    /*private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String WXOPENID = "WXOPENID";*/
    @SuppressLint("ShowToast")
    private void getAccessToken(final String code) {
        /*SharedPreferences WX_Sp = getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String code_ = WX_Sp.getString(KEY, "");
        final SharedPreferences.Editor WX_SpEditor = WX_Sp.edit();*/
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID
                + "&secret=" + APP_SECRET
                + "&code=" + code
                + "&grant_type=authorization_code";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject != null) {
                            String openid = jsonObject.getString("openid");
                            String access_token = jsonObject.getString("access_token");
                            /*if (!access_token.equals("")) {
                                WX_SpEditor.putString(ACCESS_TOKEN, access_token);
                                WX_SpEditor.commit();
                            }
                            if (!openid.equals("")) {
                                WX_SpEditor.putString(WXOPENID, openid);
                                WX_SpEditor.commit();
                                //ThirdLoginWeChat(access_token, openid);
                            }*/
                            if ((openid != null && !openid.equals("")) && (access_token != null && !access_token.equals(""))) {
                                Message msg = handler.obtainMessage();
                                msg.what = RETURN_OPENID_ACCESSTOKEN;
                                Bundle bundle = new Bundle();
                                bundle.putString("openid", openid.trim());
                                bundle.putString("access_token", access_token.trim());
                                msg.obj = bundle;
                                handler.sendMessage(msg);
                            } else {
                                ShowToast.normalShow(context, "微信登录失败，请重新点击!", true);
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        queue.add(stringRequest);
    }

    //获取access_token后，进行接口调用,客户端提取到微信关于用户的昵称，城市，省份，国家，头像，性别信息
    private void getUserinfo(final String openId, final String accessToken) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId;
        StringRequest stringRequest = new MyStringRequest(url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        JSONObject jsonObject = JSON.parseObject(response);
                        userInfo = new UserInfo();
                        if (jsonObject != null) {
                            //昵称
                            String name = EmojiFilter.filterEmoji(jsonObject.getString("nickname"));
                            userInfo.setNickname((name != null && !name.equals("")) ? name : "无名");
                            //城市
                            userInfo.setCity(jsonObject.getString("city"));
                            //省份
                            userInfo.setProvince(jsonObject.getString("province"));
                            //国家
                            userInfo.setCountry(jsonObject.getString("country"));
                            //头像地址
                            userInfo.setHeadimgurl(jsonObject.getString("headimgurl"));
                            //性别
                            userInfo.setSex(jsonObject.getString("sex"));
                            //openId
                            userInfo.setOpenId(jsonObject.getString("openid"));
                            //Toast.makeText(WXEntryActivity.this, userInfo.toString(), Toast.LENGTH_LONG).show();
                            Message msg = handler.obtainMessage();
                            msg.what = RETURN_OPENID;
                            Bundle bundle = new Bundle();
                            bundle.putString("openid", openId);//jsonObject.getString("openid").trim());
                            msg.obj = bundle;
                            handler.sendMessage(msg);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
        queue.add(stringRequest);
    }

    //如果后台存在该用户openid，则直接登录进去，否则去注册
    private void isExistOpenId(final String openid) {
        // TODO Auto-generated method stub
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_openid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        String state = jsonObject.getString("state");
                        Message msg = handler.obtainMessage();
                        msg.what = RETURN_To_Login;
                        Bundle bundle = new Bundle();
                        if (state != null && state.equals("ok")) {
                            String user = jsonObject.getString("user");
                            bundle.putString("user", user);
                            bundle.putString("isExist", "ok");
                            String token = jsonObject.getString("token");
                            String key = jsonObject.getString("key");
                            bundle.putString("token", token);
                            bundle.putString("key", key);
                            //如果是主播type = 1;用户 = 0;
                            ConstString.isLiver = (jsonObject.getIntValue("type") == 1);
                            ConstString.isLogined = true;
                        } else {
                            bundle.putString("isExist", "no");
                        }
                        msg.obj = bundle;
                        handler.sendMessage(msg);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(DIMISSION);
                ShowToast.show(context, R.string.access_fail, true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("openid", openid);
                params.put("userinfo", userInfo.toString());
                params.put("login_type", "0");
                return params;
            }
        };
        mQueue.add(postRequest);
    }

    public class MyStringRequest extends StringRequest {

        public MyStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
        }

        /**
         * 重写以解决乱码问题
         */
        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String str = null;
            try {
                str = new String(response.data, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    ////
    class MyPackageStateObserver extends IPackageStatsObserver.Stub {
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            String packageName = pStats.packageName;
            long cacheSize = pStats.cacheSize;
            long codeSize = pStats.codeSize;
            long dataSize = pStats.dataSize;
            cacheS += cacheSize;
            //
            sb.delete(0, sb.length());
            if (cacheSize > 0) {
                sb.append("packageName = " + packageName + "/n")
                        .append("   cacheSize: " + cacheSize + "/n")
                        .append("   dataSize: " + dataSize + "/n")
                        .append("-----------------------/n");
                Log.e("aaaa", sb.toString());
            }
        }
    }
    ///////
    class ClearCacheObj extends IPackageDataObserver.Stub {
        @Override
        public void onRemoveCompleted(String packageName, final boolean succeeded) throws RemoteException {
            mHadler.post(new Runnable() {
                @Override
                public void run() {
                    ShowToast.normalShow(context, "清除状态： " + succeeded, true);
                }
            });
        }
    }

    /**     * 清理全部应用程序缓存的点击事件
     *  *
     *  *
     *  */
    public void cleanAll() {
        //freeStorageAndNotify
        Method[] methods = PackageManager.class.getMethods();
        for (Method method : methods) {
            if ("freeStorageAndNotify".equals(method.getName())) {
                try {
                    method.invoke(pm, Long.MAX_VALUE, new ClearCacheObj());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }
    private void getCaches(){
        // scan
        pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        int max = packages.size();
        int current = 0;
        sb.delete(0, sb.length());
        sb.append("所有已安装的app信息：/n");
        sb.append("所有App 总和：" + max + " /n");
        //tvShowCaches.setText(sb.toString());
        for (PackageInfo pinfo : packages) {
            String packageName = pinfo.packageName;
            try {
                Method getPackageSizeInfo = PackageManager.class
                        .getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm, packageName, new MyPackageStateObserver());
                current++;
            } catch (Exception e) {
                current++;
                e.printStackTrace();
            }
        }

        //===到这里，数据准备完成
        mHadler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ShowToast.normalShow(context, "缓存信息获取完成", true);
                sbCache.append(Formatter.formatFileSize(getApplicationContext(),cacheS)+"/n");
                //tvShowCaches.setText(sb.toString());
                //tvAppCache.setText(sbCache.toString());
                sbCache.delete(0,sbCache.length());
            }
        }, 1000);
        //ok,所有应用程序信息显示完成
    }
    ///

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.setAction(ConstString.MAIN_ACTION);
            startActivity(intent);
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int version = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(version  >= 5) {
                //此为自定义的动画效果
                overridePendingTransition(R.anim.sq_en, R.anim.ex);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

