package com.zsl;

import android.app.Application;

public class MyApplication extends Application {

    //wifi提醒对话框是否不在提示
    public static boolean isWIFI = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
