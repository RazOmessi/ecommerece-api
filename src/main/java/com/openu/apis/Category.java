package com.openu.apis;

import com.openu.apis.auth.AuthManager;
import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.dal.dao.CategoryDao;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;
import com.openu.apis.utils.Roles;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;

@Path("/categories")
public class Category {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        List<String> categories = Lookups.getInstance().getLkpCategory().getAllValues(false, false);

        categories.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return Response.status(200).entity(categories).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategories(List<String> categories, @HeaderParam("Authorization") String token) {
        if(!AuthManager.getInstance().isAuthenticate(token, Roles.Admin)){
            return Response.status(403).build();
        }

        try {
            CategoryDao.getInstance().createCategories(categories);
            return Response.status(204).build();
        } catch (EcommerceException e) {
            return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
        }
    }
}
