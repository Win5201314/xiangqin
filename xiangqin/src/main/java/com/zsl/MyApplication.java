package com.zsl;

import android.app.Application;

import com.mob.MobSDK;
import com.tencent.bugly.Bugly;

import org.litepal.LitePal;

public class MyApplication extends Application {

    //wifi提醒对话框是否不在提示
    public static boolean isWIFI = true;

    @Override
    public void onCreate() {
        super.onCreate();
        MobSDK.init(this);
        LitePal.initialize(this);

        //最后一个参数,建议在测试阶段建议设置成true，发布时设置为false
        Bugly.init(getApplicationContext(), "d44a123152", false);
        //CrashReport.initCrashReport(getApplicationContext(), "d44a123152", true);
    }

}
