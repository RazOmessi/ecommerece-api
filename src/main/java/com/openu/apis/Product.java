package com.openu.apis;

import com.openu.apis.beans.ProductBean;
import com.openu.apis.configurations.ConfigurationManager;
import com.openu.apis.dal.dao.ProductDao;
import com.openu.apis.lookups.Lookups;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("/products")
public class Product {

    //@DefaultValue("2") @QueryParam("step") int step

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(@QueryParam("vendor") List<String> vendors) {
        System.out.println(vendors);
        try {
            List<ProductBean> res = ProductDao.getInstance().getAllProducts(vendors);
            return Response.status(200).entity(res).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(ProductBean product) {
        int key = Lookups.getInstance().getLkpVendor().getReversedLookup(product.getVendor());
        return Response.status(200).entity(key).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") int id) {
        ProductBean product = ProductDao.getInstance().getProductById(id);
        if(product != null){
            ConfigurationManager.getInstance();
            return Response.status(200).entity(product).build();
        }
        return Response.status(404).build();
    }



}
