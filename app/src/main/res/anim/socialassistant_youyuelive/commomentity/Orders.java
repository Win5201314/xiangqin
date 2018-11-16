package com.socialassistant_youyuelive.commomentity;

import java.io.Serializable;
import java.util.List;

/**
 * 订单实体类
 */

public class Orders implements Serializable {
    //订单号
    private String recordId;
    //消费金额
    private String pay;
    //时间
    private String time;
    //类型: 0--聊天 1--消费 2--充值 3--提现
    private String type;
    //主播昵称
    private String nickName;
    //主播头像
    private String faceUrl;
    //主播ID
    private String amchorId;
    //消息
    private String message;
    //未读消息
    private int unread;
    //用户头像
    private String userfaceUrl;
    //用户发送的信息----这是真实的用户发送的信息
    private String usertext;

    public Orders() {
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public String getAmchorId() {
        return amchorId;
    }

    public void setAmchorId(String amchorId) {
        this.amchorId = amchorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserfaceUrl() {
        return userfaceUrl;
    }

    public void setUserfaceUrl(String userfaceUrl) {
        this.userfaceUrl = userfaceUrl;
    }

    public String getUsertext() {
        return usertext;
    }

    public void setUsertext(String usertext) {
        this.usertext = usertext;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "recordId='" + recordId + '\'' +
                ", pay='" + pay + '\'' +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", nickName='" + nickName + '\'' +
                ", faceUrl='" + faceUrl + '\'' +
                ", amchorId='" + amchorId + '\'' +
                ", message='" + message + '\'' +
                ", unread=" + unread +
                ", userfaceUrl='" + userfaceUrl + '\'' +
                ", usertext='" + usertext + '\'' +
                '}';
    }
}
