package com.zsl.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

public class JobBean extends BmobObject implements Serializable {

    //公司的名字
    private String companyName;
    //技术类别
    private int type;
    //技术要求
    private String demand;
    //薪资待遇
    private String salary;
    //办公地址
    private String address;
    //是否外包性质
    private boolean isOutsource;
    //联系方式
    private String contact;
    //是否审核通过
    private boolean isPass;
    //发布日期
    private String date;
    //其他福利
    private String other;
    //比目科技id
    private String ObjectId;

    public JobBean() {}

    public JobBean(String companyName, int type, String demand, String salary, String address, boolean isOutsource,
    String contact, boolean isPass, String date, String other, String ObjectId) {
        this.companyName = companyName;
        this.type = type;
        this.demand = demand;
        this.salary = salary;
        this.address = address;
        this.isOutsource = isOutsource;
        this.contact = contact;
        this.isPass = isPass;
        this.date = date;
        this.other = other;
        this.ObjectId = ObjectId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDemand() {
        return demand;
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOutsource() {
        return isOutsource;
    }

    public void setOutsource(boolean outsource) {
        isOutsource = outsource;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isPass() {
        return isPass;
    }

    public void setPass(boolean pass) {
        isPass = pass;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    @Override
    public String getObjectId() {
        return ObjectId;
    }

    @Override
    public void setObjectId(String objectId) {
        ObjectId = objectId;
    }

    @Override
    public String toString() {
        return "JobBean{" +
                "companyName='" + companyName + '\'' +
                ", type=" + type +
                ", demand='" + demand + '\'' +
                ", salary='" + salary + '\'' +
                ", address='" + address + '\'' +
                ", isOutsource=" + isOutsource +
                ", contact='" + contact + '\'' +
                ", isPass=" + isPass +
                ", date='" + date + '\'' +
                ", other='" + other + '\'' +
                ", ObjectId='" + ObjectId + '\'' +
                '}';
    }
}
