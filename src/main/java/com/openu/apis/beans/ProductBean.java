package com.openu.apis.beans;

public class ProductBean {
    private int id;
    private String category;
    private String vendor;
    private String name;
    private String description;
    private double price;
    private int unitsInStock;
    private int discount;

    public ProductBean(){}

    public ProductBean(int id, String category, String vendor, String name, String description, double price, int unitsInStock, int discount){
        this.id = id;
        this.category = category;
        this.vendor = vendor;
        this.name = name;
        this.description = description;
        this.price = price;
        this.unitsInStock = unitsInStock;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String categoryId) {
        this.category = categoryId;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendorId) {
        this.vendor = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getUnitsInStock() {
        return unitsInStock;
    }

    public void setUnitsInStock(int unitsInStock) {
        this.unitsInStock = unitsInStock;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }
}
