package com.openu.apis.services;

import com.openu.apis.beans.OrderProductsBean;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.dal.dao.ProductDao;

import java.util.HashSet;
import java.util.Set;

public class OrderService {
    public static Set<String> validateOrder(OrderProductsBean order){
        Set<String> errors = new HashSet<>();

        ProductBean product = ProductDao.getInstance().getProductById(order.getProductId());
        if(product == null){
            errors.add(String.format("Product id: %d was not found", order.getProductId()));
        } else {
            if(product.getUnitsInStock() < order.getAmount()){
                errors.add(String.format("Not enough units in stock for product: %s", product.getName()));
            }
        }

        return errors;
    }
}
