package com.zsl.Util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.zsl.bean.UserBean;

import org.litepal.LitePal;

import java.util.List;

public class UtilTools {

    //判断是否登录进来
    public static boolean isLogined() {
        List<UserBean> userBeans = LitePal.findAll(UserBean.class);
        return (userBeans != null && userBeans.size() == 1);
    }

    public static boolean isBoss() {
        List<UserBean> userBeans = LitePal.findAll(UserBean.class);
        if (userBeans != null && userBeans.size() == 1) {
            UserBean userBean = userBeans.get(0);
            return userBean.getPhoneNumber().equals("13480901446");
        }
        return false;
    }

    public static String loginedPhone() {
        List<UserBean> userBeans = LitePal.findAll(UserBean.class);
        if (userBeans != null && userBeans.size() == 1) {
            UserBean userBean = userBeans.get(0);
            return userBean.getPhoneNumber();
        }
        return "";
    }


    /**
     * 　　* 获取版本号
     * 　　* @return 当前应用的版本号
     *
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (systemService != null) systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }
}


