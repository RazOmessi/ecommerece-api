package com.openu.apis;

import com.openu.apis.lookups.Lookups;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
