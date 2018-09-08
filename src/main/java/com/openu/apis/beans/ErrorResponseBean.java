package com.openu.apis.beans;

public class ErrorResponseBean {
    private String message;

    public ErrorResponseBean(String message){

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
