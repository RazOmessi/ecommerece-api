package com.openu.apis;

import com.openu.apis.auth.AuthManager;
import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.beans.UserBean;
import com.openu.apis.dal.dao.UserDao;
import com.openu.apis.exceptions.CreateUserException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.services.UsersService;
import com.openu.apis.utils.Roles;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;


@Path("/users")
public class User {

    private Response createUser(UserBean user){
        Set<String> errors = UsersService.validateUser(user);
        if(errors.isEmpty()){
            try {
                return Response.status(200).entity(UserDao.getInstance().createUser(user)).build();
            } catch (CreateUserException e){
                return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
            } catch (EcommerceException e) {
                throw new RuntimeException("Error implementing create user.");
            }
        }

        return Response.status(400).entity(errors).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int id) {
        UserBean user = UserDao.getInstance().getUserById(id);
        if(user != null){
            return Response.status(200).entity(user).build();
        }
        return Response.status(404).build();
    }

    @Path("/signin")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signIn(UserBean user) {
        if(UserDao.getInstance().signIn(user)){
            String token = AuthManager.getInstance().generateToken(user.getId());
            return Response.status(200).entity(token).build();
        }

        ErrorResponseBean error = new ErrorResponseBean("Bad username or password.");
        return Response.status(200).entity(error).build();
    }

    @Path("/signup/admin")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAdminUser(UserBean user) {
        user.setRoleId(Roles.Admin.toString());
        return createUser(user);
    }

    @Path("/signup/buyer")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBuyerUser(UserBean user) {
        user.setRoleId(Roles.Buyer.toString());
        return createUser(user);
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
