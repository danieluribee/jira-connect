package com.softwaredevtools.standbot;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class RestStandbotService {

    @ComponentImport
    private final ActiveObjects ao;

    private Gson gson = new Gson();

    @Inject
    public RestStandbotService(ActiveObjects ao) {
        this.ao = ao;
    }

    /*
        will indicate if the plugin is installed
        will be used by the bot server to know if the add-on is up and running on the Jira server
     */
    @GET
    @AnonymousAllowed
    @Path("healthcheck")
    public Response healthcheck() {
        SlackIntegrationEntity[] slackIntegrations = ao.find(SlackIntegrationEntity.class);
        SlackIntegration slackIntegration;

        // if a record exists, then use its status
        if (slackIntegrations != null && slackIntegrations.length > 0) {
            slackIntegration = new SlackIntegration(slackIntegrations[0].getActive());
        }
        //otherwise, use false
        else {
            slackIntegration = new SlackIntegration(false);
        }

        return Response.ok(gson.toJson(slackIntegration)).build();
    }

    @POST
    @Path("activate")
    public Response activateIntegration() {
        return Response.ok().build();
    }

    @POST
    @Path("deactivate")
    public Response deactivate() {
        return Response.ok().build();
    }
}
