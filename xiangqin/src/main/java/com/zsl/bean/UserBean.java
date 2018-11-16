package com.zsl.bean;

import org.litepal.crud.LitePalSupport;

public class UserBean extends LitePalSupport {

    //账号
    private String phoneNumber;
    //密码
    private String password;

    public UserBean() {
    }

    public UserBean(String phoneNumber, String password) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
