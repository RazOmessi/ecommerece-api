package com.openu.apis.beans;

import java.sql.Timestamp;

public class UserAuthBean {
    private int userId;
    private String token;
    private Timestamp timestamp;

    public UserAuthBean(){}

    public UserAuthBean(int userId, String token, Timestamp timestamp){
        this.userId = userId;
        this.token = token;
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
