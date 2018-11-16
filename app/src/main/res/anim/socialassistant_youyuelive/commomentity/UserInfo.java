package com.socialassistant_youyuelive.commomentity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/4/27.
 */

public class UserInfo implements  Serializable {
    //注册方式0代表微信注册，1代表手机号码注册
    private String registrationWay;
    //授权ID
    private String openId;
    //昵称
    private String nickname;
    //城市
    private String city;
    //省份
    private String province;
    //国家
    private String country;
    //头像
    private String headimgurl;
    //性别(微信接口普通用户性别，1为男性，2为女性)
    private String sex;
    //账号(手机号码)
    private String account;
    //密码
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistrationWay() {
        return registrationWay;
    }

    public void setRegistrationWay(String registrationWay) {
        this.registrationWay = registrationWay;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public UserInfo() {
        super();
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("registrationWay", registrationWay);
        jsonObject.put("openId", openId);
        jsonObject.put("nickname", nickname);
        jsonObject.put("city", city);
        jsonObject.put("province", province);
        jsonObject.put("country", country);
        jsonObject.put("headimgurl", headimgurl);
        jsonObject.put("sex", sex);
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        return jsonObject.toString();
    }
}
