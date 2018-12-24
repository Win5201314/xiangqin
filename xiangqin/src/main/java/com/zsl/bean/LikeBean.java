package com.zsl.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

public class LikeBean extends BmobObject implements Serializable {

    //手机号
    String phone;
    //喜欢我的
    String likeMe;
    //我喜欢的
    String likeOther;

    public LikeBean() {
    }

    public LikeBean(String phone, String likeMe, String likeOther) {
        this.phone = phone;
        this.likeMe = likeMe;
        this.likeOther = likeOther;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLikeMe() {
        return likeMe;
    }

    public void setLikeMe(String likeMe) {
        this.likeMe = likeMe;
    }

    public String getLikeOther() {
        return likeOther;
    }

    public void setLikeOther(String likeOther) {
        this.likeOther = likeOther;
    }

    @Override
    public String toString() {
        return "LikeBean{" +
                "phone='" + phone + '\'' +
                ", likeMe='" + likeMe + '\'' +
                ", likeOther='" + likeOther + '\'' +
                '}';
    }
}
