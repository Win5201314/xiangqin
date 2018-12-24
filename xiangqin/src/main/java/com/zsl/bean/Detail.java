package com.zsl.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * 个人投稿信息描述
 */
public class Detail extends BmobObject implements Serializable {

    //姓名
    private String name;
    //性别
    private String sex;
    //手机号码
    private String phone;
    //三张图片[用逗号分隔]
    private String imageUrl;
    //出生年月
    private String birthday;
    //有无婚史
    private String marry;
    //家乡
    private String path;
    //现居地
    private String residence;
    //身高
    private String height;
    //体重
    private String weight;
    //星座
    //private String constellation;
    //学历
    private String education;
    //职业工作
    private String occupation;
    //平均月薪
    private String salary;
    //车房情况
    private String car;
    //个性描述
    //private String character;
    //对另一半的要求
    private String requirement;
    //个人规划
    //private String planning;
    //家庭成员
    //private String family;
    //邮箱地址
    //private String email;
    //备注[额外补充]
    private String bc;

    public Detail() {
    }

    public Detail(String name, String sex, String phone, String imageUrl, String birthday,
                  String marry, String path, String residence, String height, String weight,
                  String education, String occupation, String salary, String car,
                  String requirement, String bc) {
        this.name = name;
        this.sex = sex;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.birthday = birthday;
        this.marry = marry;
        this.path = path;
        this.residence = residence;
        this.height = height;
        this.weight = weight;
        this.education = education;
        this.occupation = occupation;
        this.salary = salary;
        this.car = car;
        this.requirement = requirement;
        this.bc = bc;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBc() {
        return bc;
    }

    public void setBc(String bc) {
        this.bc = bc;
    }

    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getMarry() {
        return marry;
    }
    public void setMarry(String marry) {
        this.marry = marry;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getResidence() {
        return residence;
    }
    public void setResidence(String residence) {
        this.residence = residence;
    }
    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }
    public String getWeight() {
        return weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
    public String getEducation() {
        return education;
    }
    public void setEducation(String education) {
        this.education = education;
    }
    public String getOccupation() {
        return occupation;
    }
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    public String getSalary() {
        return salary;
    }
    public void setSalary(String salary) {
        this.salary = salary;
    }
    public String getCar() {
        return car;
    }
    public void setCar(String car) {
        this.car = car;
    }
    public String getRequirement() {
        return requirement;
    }
    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", phone='" + phone + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", birthday='" + birthday + '\'' +
                ", marry='" + marry + '\'' +
                ", path='" + path + '\'' +
                ", residence='" + residence + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", education='" + education + '\'' +
                ", occupation='" + occupation + '\'' +
                ", salary='" + salary + '\'' +
                ", car='" + car + '\'' +
                ", requirement='" + requirement + '\'' +
                ", bc='" + bc + '\'' +
                '}';
    }
}

