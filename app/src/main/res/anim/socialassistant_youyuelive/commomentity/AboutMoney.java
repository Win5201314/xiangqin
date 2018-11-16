package com.socialassistant_youyuelive.commomentity;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/7/27.
 */

public class AboutMoney extends DataSupport {
    //类型  0代表聊天的  1代表赞赏的  2代表视频语音消费的
    private String type;
    //主播昵称
    private String nickName;
    //主播头像url
    private String faceUrl;
    //主播ID
    private String anchorId;
    //订单id
    private String recordId;
    //消费金额
    private String pay;
    //时间
    private String time;
    //类型为0的时候有这个字段  聊天的第一句话
    private String message;
    //0主播，1普通用户
    private String roleType;
    //已读和未读
    private String readsum;
    //主播说话次数
    private String talksum;
    //用户说的话
    private String usermessage;
    //用户的头像
    private String userfaceUrl;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getReadsum() {
        return readsum;
    }

    public void setReadsum(String readsum) {
        this.readsum = readsum;
    }

    public String getTalksum() {
        return talksum;
    }

    public void setTalksum(String talksum) {
        this.talksum = talksum;
    }

    public String getUsermessage() {
        return usermessage;
    }

    public void setUsermessage(String usermessage) {
        this.usermessage = usermessage;
    }

    public String getUserfaceUrl() {
        return userfaceUrl;
    }

    public void setUserfaceUrl(String userfaceUrl) {
        this.userfaceUrl = userfaceUrl;
    }
}
