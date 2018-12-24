package com.zsl.room.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

public class QZBean extends BmobObject implements Serializable {
    //商圈
    private String sq;
    //房源地址要求
    private String yq;
    //价格区间
    private String money;
    //是否需要高楼层
    private String gao;
    //个人标签及需求
    private String xq;
    //是否携带宠物
    private String cw;

    public QZBean() {
    }

    public QZBean(String sq, String yq, String money, String gao, String xq, String cw) {
        this.sq = sq;
        this.yq = yq;
        this.money = money;
        this.gao = gao;
        this.xq = xq;
        this.cw = cw;
    }

    public String getSq() {
        return sq;
    }

    public void setSq(String sq) {
        this.sq = sq;
    }

    public String getYq() {
        return yq;
    }

    public void setYq(String yq) {
        this.yq = yq;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getGao() {
        return gao;
    }

    public void setGao(String gao) {
        this.gao = gao;
    }

    public String getXq() {
        return xq;
    }

    public void setXq(String xq) {
        this.xq = xq;
    }

    public String getCw() {
        return cw;
    }

    public void setCw(String cw) {
        this.cw = cw;
    }

    @Override
    public String toString() {
        return "QZBean{" +
                "sq='" + sq + '\'' +
                ", yq='" + yq + '\'' +
                ", money='" + money + '\'' +
                ", gao='" + gao + '\'' +
                ", xq='" + xq + '\'' +
                ", cw='" + cw + '\'' +
                '}';
    }
}
