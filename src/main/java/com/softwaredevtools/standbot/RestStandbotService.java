package com.softwaredevtools.standbot;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class RestStandbotService {

    /*
        will indicate if the plugin is installed
        will be used by the bot server to know if the add-on is up and running on the Jira server
     */
    @GET
    @AnonymousAllowed
    @Path("healthcheck")
    public Response getUncompletedUsers() {
        return Response.ok().build();
    }
}