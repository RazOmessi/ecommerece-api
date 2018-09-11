package com.openu.apis;

import com.openu.apis.auth.AuthManager;
import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.dal.dao.ProductDao;
import com.openu.apis.exceptions.ProductDaoException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.services.ProductsService;
import com.openu.apis.utils.Roles;

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
    public Response getAllProducts(@QueryParam("vendor") List<String> vendors, @QueryParam("category") List<String> categories) {
        try {
            List<ProductBean> res = ProductDao.getInstance().getProducts(vendors, categories);
            return Response.status(200).entity(res).build();
        } catch (SQLException e) {
            //todo: add logger
            e.printStackTrace();
            return Response.serverError().entity(new ErrorResponseBean(e.getMessage())).build();
        } catch (ProductDaoException e) {
            //todo: add logger
            e.printStackTrace();
            return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(ProductBean product, @HeaderParam("Authorization") String token) {
        if(!AuthManager.getInstance().isAuthenticate(token, Roles.Admin)){
            return Response.status(403).build();
        }

        Set<String> errors = ProductsService.validateProduct(product);
        if(errors.isEmpty()){
            try {
                return Response.status(200).entity(ProductDao.getInstance().createProduct(product)).build();
            } catch (ProductDaoException e){
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
