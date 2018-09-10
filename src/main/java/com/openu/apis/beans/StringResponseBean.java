package com.openu.apis.beans;

public class StringResponseBean {
    private String value;

    public StringResponseBean(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
