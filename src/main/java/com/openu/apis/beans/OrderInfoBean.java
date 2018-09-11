package com.openu.apis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfoBean {

    private int id;
    private int userId;
    private Timestamp timestamp;
    private String status;
    private List<OrderProductsBean> products;

    public OrderInfoBean(){}

    public OrderInfoBean(int id, int userId, Timestamp timestamp, String status, List<OrderProductsBean> products){
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
        this.status = status;
        this.products = products;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderProductsBean> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProductsBean> products) {
        this.products = products;
    }
}
