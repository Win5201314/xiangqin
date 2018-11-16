package com.socialassistant_youyuelive.commomentity;


public class Message {
    public int friendImage;
    public String friendName;
    public String friendSign;
    public String friendTime;
    public boolean isLive;

    public Message(int friendImage, String friendName, String friendSign, String friendTime, boolean isLive) {
        this.friendImage = friendImage;
        this.friendName = friendName;
        this.friendSign = friendSign;
        this.friendTime = friendTime;
        this.isLive = isLive;
    }

    @Override
    public String toString() {
        return "Message{" +
                "friendImage=" + friendImage +
                ", friendName='" + friendName + '\'' +
                ", friendSign='" + friendSign + '\'' +
                ", friendTime='" + friendTime + '\'' +
                ", isLive=" + isLive +
                '}';
    }

    public String getFriendTime() {
        return friendTime;
    }

    public void setFriendTime(String friendTime) {
        this.friendTime = friendTime;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }


    public int getFriendImage() {
        return friendImage;
    }

    public void setFriendImage(int friendImage) {
        this.friendImage = friendImage;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendSign() {
        return friendSign;
    }

    public void setFriendSign(String friendSign) {
        this.friendSign = friendSign;
    }


}
