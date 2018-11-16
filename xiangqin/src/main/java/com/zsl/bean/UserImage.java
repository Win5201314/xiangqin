package com.zsl.bean;

public class UserImage {

    //主图片
    private String imageUrl;
    //家乡
    private String jx;
    //现居地
    private String xjd;
    //出生年月
    private String birthday;

    public UserImage() {
    }

    public UserImage(String imageUrl, String jx, String xjd, String birthday) {
        this.imageUrl = imageUrl;
        this.jx = jx;
        this.xjd = xjd;
        this.birthday = birthday;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getJx() {
        return jx;
    }

    public void setJx(String jx) {
        this.jx = jx;
    }

    public String getXjd() {
        return xjd;
    }

    public void setXjd(String xjd) {
        this.xjd = xjd;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
