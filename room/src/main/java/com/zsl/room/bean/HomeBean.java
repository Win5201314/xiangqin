package com.zsl.room.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

public class HomeBean extends BmobObject implements Serializable {

    //商圈
    private String sq;
    //房源地址
    private String address;
    //是否小区房
    private String isXiaoQu;
    //是否有电梯
    private String isDianTi;
    //房源价格
    private String money;
    //房源情况
    private String qk;
    //类型
    private String type;
    //图片地址
    private String image;

    public HomeBean() {
    }

    public HomeBean(String sq, String address, String isXiaoQu, String isDianTi, String money, String qk, String type, String image) {
        this.sq = sq;
        this.address = address;
        this.isXiaoQu = isXiaoQu;
        this.isDianTi = isDianTi;
        this.money = money;
        this.qk = qk;
        this.type = type;
        this.image = image;
    }

    public String getSq() {
        return sq;
    }

    public void setSq(String sq) {
        this.sq = sq;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsXiaoQu() {
        return isXiaoQu;
    }

    public void setIsXiaoQu(String isXiaoQu) {
        this.isXiaoQu = isXiaoQu;
    }

    public String getIsDianTi() {
        return isDianTi;
    }

    public void setIsDianTi(String isDianTi) {
        this.isDianTi = isDianTi;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getQk() {
        return qk;
    }

    public void setQk(String qk) {
        this.qk = qk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        String s = "商圈:" + sq + "\n"
                + "房源地址:" + address + "\n"
                + "是否小区房:" + isXiaoQu + "\n"
                + "是否有电梯:" + isDianTi + "\n"
                + "房源价格:" + money + "\n"
                + "房源情况:" + qk + "\n";
        return s;
    }
}
