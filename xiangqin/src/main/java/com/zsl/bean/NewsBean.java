package com.zsl.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

public class NewsBean extends BmobObject implements Serializable {

    //日期
    private String data;
    //内容
    private String news;

    public NewsBean() {
    }

    public NewsBean(String data, String news) {
        this.data = data;
        this.news = news;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }

    @Override
    public String toString() {
        return "NewsBean{" +
                "data='" + data + '\'' +
                ", news='" + news + '\'' +
                '}';
    }
}
