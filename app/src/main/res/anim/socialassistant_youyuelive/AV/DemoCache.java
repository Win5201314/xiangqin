package com.socialassistant_youyuelive.AV;

import android.content.Context;

import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by zjm on 2017/4/21.
 */

// 用来缓存账号信息
public class DemoCache {

    private static Context context;

    private static String account;

    private static String showHeadImgUri;

    private static StatusBarNotificationConfig notificationConfig;

    private static String accountId;

    private static String AnchorId;

    private static String accountHeadImg;

    private static String accountNickName;

    private static String anchorNickName;

    private static boolean isLiver;

    private static boolean isLoginYunxin;

    public static void clear() {
        account = null;
        accountId = null;
        accountHeadImg = null;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        DemoCache.account = account;
//        NimUIKit.setAccount(account);
    }

    public static void setNotificationConfig(StatusBarNotificationConfig notificationConfig) {
        DemoCache.notificationConfig = notificationConfig;
    }

    public static StatusBarNotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        DemoCache.context = context.getApplicationContext();
    }

    public static String getShowHeadImgUri() {
        return showHeadImgUri;
    }

    public static void setShowHeadImgUri(String showHeadImgUri) {
        DemoCache.showHeadImgUri = showHeadImgUri;
    }

    public static String getAccountHeadImg() {
        return accountHeadImg;
    }

    public static void setAccountHeadImg(String accountHeadImg) {
        DemoCache.accountHeadImg = accountHeadImg;
    }

    public static String getAccountId() {
        return accountId;
    }

    public static void setAccountId(String accountId) {
        DemoCache.accountId = accountId;
    }

    public static String getAccountNickName() {
        return accountNickName;
    }

    public static void setAccountNickName(String accountNickName) {
        DemoCache.accountNickName = accountNickName;
    }

    public static String getAnchorNickName() {
        return anchorNickName;
    }

    public static void setAnchorNickName(String anchorNickName) {
        DemoCache.anchorNickName = anchorNickName;
    }

    public static void setAnchorId(String anchorId) {
        AnchorId = anchorId;
    }

    public static String getAnchorId() {
        return AnchorId;
    }

    public static void setIsLiver(boolean isLiver) {
        DemoCache.isLiver = isLiver;
    }

    public static boolean isIsLiver() {
        return isLiver;
    }

    public static void setIsLoginYunxin(boolean isLoginYunxin) {
        DemoCache.isLoginYunxin = isLoginYunxin;
    }

    public static boolean getIsLoginYunxin() {
        return isLoginYunxin;
    }
}
