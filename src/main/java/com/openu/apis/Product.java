package com.openu.apis;

import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.configurations.ConfigurationManager;
import com.openu.apis.dal.dao.ProductDao;
import com.openu.apis.exceptions.CreateProductException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;
import com.openu.apis.services.ProductsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Path("/products")
public class Product {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(@QueryParam("vendor") List<String> vendors) {
        System.out.println(vendors);
        try {
            List<ProductBean> res = ProductDao.getInstance().getProducts(vendors);
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
        Set<String> errors = ProductsService.validateProduct(product);
        if(errors.isEmpty()){
            try {
                return Response.status(200).entity(ProductDao.getInstance().createProduct(product)).build();
            } catch (CreateProductException e){
                return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
            } catch (EcommerceException e) {
                throw new RuntimeException("Error implementing create product.");
            }
        }

        return Response.status(400).entity(errors).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") int id) {
        ProductBean product = ProductDao.getInstance().getProductById(id);
        if(product != null){
            return Response.status(200).entity(product).build();
        }
        return Response.status(404).build();
    }

}
