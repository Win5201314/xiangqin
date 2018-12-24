package com.zsl.room.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.room.MyApplication;
import com.zsl.room.R;
import com.zsl.room.util.ActivityCollector;
import com.zsl.room.util.Logger;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/5/5.
 */

public class BaseActivity extends AppCompatActivity implements SlidingPaneLayout.PanelSlideListener {

    private ConnectionReceiver connectionReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        initSwipeBackFinish();
        //监听WiFi状态，因为本软件会消耗大量流量,提示用户
        if (MyApplication.isWIFI) {
            connectionReceiver = new ConnectionReceiver();
            IntentFilter intentFilter = new IntentFilter();
            //intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            registerReceiver(connectionReceiver, intentFilter);
        }
    }

    /**
     * 初始化滑动返回
     */
    private void initSwipeBackFinish() {
        if (isSupportSwipeBack()) {
            SlidingPaneLayout slidingPaneLayout = new SlidingPaneLayout(this);
            //通过反射改变mOverhangSize的值为0，这个mOverhangSize值为菜单到右边屏幕的最短距离，默认
            //是32dp，现在给它改成0
            try {
                //属性
                Field f_overHang = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
                f_overHang.setAccessible(true);
                f_overHang.set(slidingPaneLayout, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            slidingPaneLayout.setPanelSlideListener(this);
            slidingPaneLayout.setSliderFadeColor(getResources().getColor(android.R.color.transparent));

            View leftView = new View(this);
            leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            slidingPaneLayout.addView(leftView, 0);

            ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
            decorChild.setBackgroundColor(getResources().getColor(android.R.color.white));
            decor.removeView(decorChild);
            decor.addView(slidingPaneLayout);
            slidingPaneLayout.addView(decorChild, 1);
        }
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean isSupportSwipeBack() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
        }
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelOpened(View panel) {
        finish();
        this.overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    public void onPanelClosed(View panel) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
        int version = Integer.valueOf(Build.VERSION.SDK);
        if(version  >= 5) {
            //此为自定义的动画效果
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                int v1 = Integer.valueOf(Build.VERSION.SDK);
                if(v1 >= 5) {
                    //此为自定义的动画效果
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    //由于软件会加载很多图片和大视频，特地监控是否为WIFI状态使用本软件，以提醒用户
    public class ConnectionReceiver extends BroadcastReceiver {

        private final static String TAG = "WIFI";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (TextUtils.isEmpty(intent.getAction())) return;
            if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
                //signal strength changed
            } else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
                Logger.d(TAG, "网络状态改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                    Logger.d(TAG, "wifi网络连接断开");
                    showWIFIDialog(context);
                } else if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称和是否可用
                    boolean canUse = isWifiConnected(context);
                    Logger.d(TAG, "连接到网络 " + wifiInfo.getSSID());
                    Logger.d(TAG, "是否可用 " + canUse);
                    //连上了，但是网络无效
                    if (!canUse) showWIFIDialog(context);
                }
            } else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                    Logger.d(TAG, "系统关闭wifi");
                } else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                    Logger.d(TAG, "系统开启wifi");
                }
            }
        }

        //判断WIFI网络是否可用
        public boolean isWifiConnected(Context context) {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWiFiNetworkInfo != null) {
                    return mWiFiNetworkInfo.isAvailable();
                }
            }
            return false;
        }

        public void showWIFIDialog(Context context) {
            new AlertDialog.Builder(context)
                    .setTitle("温馨提醒")
                    .setMessage("目前的WiFi处于断开或者无效!")
                    .setCancelable(true)
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MyApplication.isWIFI = false;
                        }
                    }).show();
        }

    }

}
