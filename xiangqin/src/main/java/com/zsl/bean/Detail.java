package com.zsl.bean;

/**
 * 个人投稿信息描述
 */
public class Detail {

    //姓名
    private String name;
    //性别
    private String sex;
    //手机号码
    private String phone;
    //三张图片
    private String image1;
    private String image2;
    private String image3;
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
    private String constellation;
    //学历
    private String education;
    //职业工作
    private String occupation;
    //平均月薪
    private String salary;
    //车房情况
    private String car;
    //个性描述
    private String character;
    //对另一半的要求
    private String requirement;
    //个人规划
    private String planning;
    //家庭成员
    private String family;
    //邮箱地址
    private String email;

    public Detail() {
    }

    public Detail(String name, String sex, String phone, String image1, String image2, String image3, String birthday, String marry, String path, String residence, String height, String weight, String constellation, String education, String occupation, String salary, String car, String character, String requirement, String planning, String family, String email) {
        this.name = name;
        this.sex = sex;
        this.phone = phone;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.birthday = birthday;
        this.marry = marry;
        this.path = path;
        this.residence = residence;
        this.height = height;
        this.weight = weight;
        this.constellation = constellation;
        this.education = education;
        this.occupation = occupation;
        this.salary = salary;
        this.car = car;
        this.character = character;
        this.requirement = requirement;
        this.planning = planning;
        this.family = family;
        this.email = email;
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

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
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
    public String getConstellation() {
        return constellation;
    }
    public void setConstellation(String constellation) {
        this.constellation = constellation;
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
    public String getCharacter() {
        return character;
    }
    public void setCharacter(String character) {
        this.character = character;
    }
    public String getRequirement() {
        return requirement;
    }
    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
    public String getPlanning() {
        return planning;
    }
    public void setPlanning(String planning) {
        this.planning = planning;
    }
    public String getFamily() {
        return family;
    }
    public void setFamily(String family) {
        this.family = family;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", phone='" + phone + '\'' +
                ", image1='" + image1 + '\'' +
                ", image2='" + image2 + '\'' +
                ", image3='" + image3 + '\'' +
                ", birthday='" + birthday + '\'' +
                ", marry='" + marry + '\'' +
                ", path='" + path + '\'' +
                ", residence='" + residence + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", constellation='" + constellation + '\'' +
                ", education='" + education + '\'' +
                ", occupation='" + occupation + '\'' +
                ", salary='" + salary + '\'' +
                ", car='" + car + '\'' +
                ", character='" + character + '\'' +
                ", requirement='" + requirement + '\'' +
                ", planning='" + planning + '\'' +
                ", family='" + family + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

