package com.openu.apis;

import com.openu.apis.beans.ErrorResponseBean;
import com.openu.apis.beans.OrderBean;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.dal.dao.OrderDao;
import com.openu.apis.dal.dao.ProductDao;
import com.openu.apis.exceptions.OrderDAOException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.services.OrderService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;


@Path("/orders")
public class Order {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrders(@QueryParam("userid") Integer userId) {
        try {
            List<OrderBean> res = OrderDao.getInstance().getOrders(userId);
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
    public Response createOrder(OrderBean order) {
        Set<String> errors = OrderService.validateOrder(order);
        if(errors.isEmpty()){
            try {
                return Response.status(200).entity(OrderDao.getInstance().createOrder(order)).build();
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
    public Response getOrder(@PathParam("id") int id) {
        OrderBean order = OrderDao.getInstance().getOrderById(id);
        if(order != null){
            return Response.status(200).entity(order).build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrderStatus(OrderBean order, @PathParam("id") int id) {
        order.setId(id);
        //todo: validate order update

        return updateStatus(order);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelOrder(@PathParam("id") int id) {
        OrderBean order = new OrderBean();
        order.setId(id);
        //todo: replace with const or something
        order.setStatus("Canceled");

        return updateStatus(order);
    }

    private Response updateStatus(OrderBean order){
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
