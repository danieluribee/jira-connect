package com.softwaredevtools.standbot;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class RestStandbotService {
    @GET
    @Path("test")
    public Response getUncompletedUsers() {
        return Response.ok("works!").build();
    }
}