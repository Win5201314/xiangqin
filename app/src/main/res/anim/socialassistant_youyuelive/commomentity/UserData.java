package com.socialassistant_youyuelive.commomentity;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/7/25.
 */

public class UserData extends DataSupport {

    //登录后的个人数据(个人或者主播)json数据
    private String userData;
    //是否是主播(默认false，不是主播)
    private boolean isLiver;
    //是否登录(默认false,没有登录)
    private boolean isLogined;
    //验证需要的API_KEY
    private String token;
    private String key;
    //手机号或者是openId
    private String mobile;

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public boolean isLiver() {
        return isLiver;
    }

    public void setLiver(boolean liver) {
        isLiver = liver;
    }

    public boolean isLogined() {
        return isLogined;
    }

    public void setLogined(boolean logined) {
        isLogined = logined;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
