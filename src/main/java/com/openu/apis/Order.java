package com.openu.apis;

import com.openu.apis.auth.AuthManager;
import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.beans.OrderInfoBean;
import com.openu.apis.beans.OrderProductsBean;
import com.openu.apis.dal.dao.OrderDao;
import com.openu.apis.exceptions.OrderDAOException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.services.OrderService;
import com.openu.apis.utils.Roles;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Path("/orders")
public class Order {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrders(@QueryParam("userid") Integer userId, @HeaderParam("Authorization") String token) {
        if(!(AuthManager.getInstance().isAuthenticate(token, Roles.Admin) || (userId != null && AuthManager.getInstance().isAuthenticate(token, Roles.Buyer, userId)))){
            return Response.status(403).build();
        }

        try {
            List<OrderInfoBean> res = OrderDao.getInstance().getOrders(userId);
            return Response.status(200).entity(res).build();
        } catch (SQLException e) {
            //todo: add logger
            ErrorResponseBean error = new ErrorResponseBean(e.getMessage());
            return Response.serverError().entity(error).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(@HeaderParam("Authorization") String token, OrderInfoBean order) {
        if(!AuthManager.getInstance().isAuthenticate(token, Roles.Buyer)){
            return Response.status(403).build();
        }

        Set<String> errors = order.getProducts().stream().flatMap(opb -> OrderService.validateOrder(opb).stream()).collect(Collectors.toSet());
        if(errors.isEmpty()){
            try {
                OrderDao.getInstance().createOrders(AuthManager.getInstance().getUserId(token), order);
                return Response.status(204).build();
            } catch (OrderDAOException e){
                return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
            } catch (EcommerceException e) {
                throw new RuntimeException("Error implementing create order.");
            }
        }

        return Response.status(400).entity(errors).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") int id, @HeaderParam("Authorization") String token) {
        OrderInfoBean order = OrderDao.getInstance().getOrderById(id);
        if(order != null){
            if(!(AuthManager.getInstance().isAuthenticate(token, Roles.Admin) || AuthManager.getInstance().isAuthenticate(token, Roles.Buyer, order.getUserId()))){
                return Response.status(403).build();
            }

            return Response.status(200).entity(order).build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrderStatus(OrderInfoBean order, @PathParam("id") int id, @HeaderParam("Authorization") String token) {
        if(!AuthManager.getInstance().isAuthenticate(token, Roles.Admin)){
            return Response.status(403).build();
        }

        order.setId(id);
        //todo: validate order update

        return updateStatus(order);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelOrder(@PathParam("id") int id, @HeaderParam("Authorization") String token) {
        OrderInfoBean order = OrderDao.getInstance().getOrderById(id);
        if(!(AuthManager.getInstance().isAuthenticate(token, Roles.Admin) || (AuthManager.getInstance().isAuthenticate(token, Roles.Buyer, order.getUserId())))){
            return Response.status(403).build();
        }

        //todo: replace with const or something
        order.setStatus("Canceled");

        return updateStatus(order);
    }

    private Response updateStatus(OrderInfoBean order){
        try {
            if(OrderDao.getInstance().updateOrder(order)){
                return Response.status(204).build();
            } else {
                return Response.status(404).build();
            }
        } catch (OrderDAOException e) {
            return Response.status(400).entity(new ErrorResponseBean(e.getMessage())).build();
        }
    }
}
