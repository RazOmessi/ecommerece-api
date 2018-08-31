package com.openu.apis;

import com.openu.apis.beans.ProductBean;
import com.openu.apis.configurations.ConfigurationManager;
import com.openu.apis.dal.dao.ProductDao;
import com.openu.apis.lookups.Lookups;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/products")
public class Product {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts() {
        try {
            List<ProductBean> res = ProductDao.getInstance().getAllProducts();
            return Response.status(200).entity(res).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") int id) {
        ProductBean product = ProductDao.getInstance().getProductById(id);
        if(product != null){
            String vendor = Lookups.getInstance().getLkpVendor().getLookup(product.getVendorId());
            ConfigurationManager.getInstance().touch();
            return Response.status(200).entity(vendor).build();
            //return Response.status(200).entity(product).build();
        }
        return Response.status(404).build();
    }



}
