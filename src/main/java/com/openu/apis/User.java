package com.openu.apis;

import com.openu.apis.beans.UserBean;
import com.openu.apis.dal.dao.UserDao;
import com.openu.apis.services.UsersService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;


@Path("/users")
public class User {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(UserBean user) {


        return Response.status(200).entity(UsersService.validateUser(user)).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        try {
            List<UserBean> res = UserDao.getInstance().getAllUser();
            return Response.status(200).entity(res).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
