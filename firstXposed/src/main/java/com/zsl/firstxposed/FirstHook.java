package com.zsl.firstxposed;

import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 抖音方面的
 * 版本3.4.0
 */
public class FirstHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals("com.ss.android.ugc.aweme")) {
            XposedBridge.log("--------框架生效");
            //阻止更新对话框的弹出
            stopUpdateDialog(lpparam);
            //修改签名
            changeSignature(lpparam);
            //点赞
            like(lpparam);
        }

    }

    private void like(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("点赞!");
        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.detail.ui.DetailActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(param.thisObject,"a", false);
            }
        });
    }

    Object object;
    private void changeSignature(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("修改个性签名!");
        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.profile.ui.ProfileEditSignatureFragment", lpparam.classLoader, "onClearInput", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                object = param.thisObject;
                EditText editText = (EditText) XposedHelpers.getObjectField(param.thisObject,"mSignatureInput");
                editText.setText("zsl13563");
                XposedHelpers.callMethod(object,"c");
                return null;
            }
        });

    }

    private void stopUpdateDialog(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("阻止更新对话框的弹出!");
        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.update.k$d", lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Message m = (Message) param.args[0];
                XposedBridge.log("调用了handleMessage"+m.what);
                if (m.what == 6) {
                    m.what = 999;
                    param.args[0] = m;
                }

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

            }
        });
    }

}
