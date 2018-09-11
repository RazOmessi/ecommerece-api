package com.openu.apis.beans;

public class IdTokenBean {
    private int userId;
    private String value;

    public IdTokenBean(int userId, String value){
        this.userId = userId;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
