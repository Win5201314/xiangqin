package com.socialassistant_youyuelive.commomentity;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/8.
 */

public class UserID implements Serializable {
    private String openid;
    //头像url
    private String face_url;
    //是否正在直播
    private boolean isLive;
    //昵称
    private String nickName;
    //性别      : sex(false女 true男)
    private boolean isMan;
    //年龄
    private int years;
    //签名
    private String signature;
    //相册最多5张，根据逗号分割
    private String album;
    //主播ID
    private int anchorId;
    //主播等级
    private int anchorLevel;
    //城市
    private String city;
    //省份
    private String province;
    //国家
    private String country;
    //关注人数
    private int friendsAccount;
    //账号
    private String mobile;
    //tokenId
    private String tokenId;
    //极光推送ID
    private String JPush_ID;
    //聊天状态（0视频中 1可视频 2免打扰）
    private int chat_status;
    //标签
    private String labels;
    //点数
    private int points;
    //时间
    private  String time;
    //语音
    private String voiceUrl;
    //跟主播的距离
    private float distance;
    //主播视频
    private String headVedio;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(int anchorId) {
        this.anchorId = anchorId;
    }

    public int getAnchorLevel() {
        return anchorLevel;
    }

    public void setAnchorLevel(int anchorLevel) {
        this.anchorLevel = anchorLevel;
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

    public int getFriendsAccount() {
        return friendsAccount;
    }

    public void setFriendsAccount(int friendsAccount) {
        this.friendsAccount = friendsAccount;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getJPush_ID() {
        return JPush_ID;
    }

    public void setJPush_ID(String JPush_ID) {
        this.JPush_ID = JPush_ID;
    }

    public int getChat_status() {
        return chat_status;
    }

    public void setChat_status(int chat_status) {
        this.chat_status = chat_status;
    }

    public UserID() {
    }

    public UserID(String face_url, boolean isLive, String nickName, boolean isMan, int years, String signature) {
        this.face_url = face_url;
        this.isLive = isLive;
        this.nickName = nickName;
        this.isMan = isMan;
        this.years = years;
        this.signature = signature;
    }

    public String getFace_url() {
        return face_url;
    }

    public boolean isLive() {
        return isLive;
    }

    public String getNickName() {
        return nickName;
    }

    public boolean isMan() {
        return isMan;
    }

    public int getYears() {
        return years;
    }

    public String getSignature() {
        return signature;
    }

    public void setFace_url(String face_url) {
        this.face_url = face_url;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setMan(boolean man) {
        isMan = man;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getHeadVedio() {
        return headVedio;
    }

    public void setHeadVedio(String headVedio) {
        this.headVedio = headVedio;
    }

    @Override
    public String toString() {
        return "UserID{" +
                "openid='" + openid + '\'' +
                ", face_url='" + face_url + '\'' +
                ", isLive=" + isLive +
                ", nickName='" + nickName + '\'' +
                ", isMan=" + isMan +
                ", years=" + years +
                ", signature='" + signature + '\'' +
                ", album='" + album + '\'' +
                ", anchorId=" + anchorId +
                ", anchorLevel=" + anchorLevel +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                ", friendsAccount=" + friendsAccount +
                ", mobile='" + mobile + '\'' +
                ", tokenId='" + tokenId + '\'' +
                ", JPush_ID='" + JPush_ID + '\'' +
                ", chat_status=" + chat_status +
                ", labels='" + labels + '\'' +
                ", points=" + points +
                ", time='" + time + '\'' +
                ", voiceUrl='" + voiceUrl + '\'' +
                ", distance=" + distance +
                ", headVedio='" + headVedio + '\'' +
                '}';
    }
}
