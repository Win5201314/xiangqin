package com.socialassistant_youyuelive.entity;

/**
 * Created by user10 on 2017/7/31.
 */

public class WithDrawEntity {

    /**
     * status : true
     * message : 申请提现成功
     * values : 1
     */

    private boolean status;
    private String message;
    private String values;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "WithDrawEntity{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", values='" + values + '\'' +
                '}';
    }
}
