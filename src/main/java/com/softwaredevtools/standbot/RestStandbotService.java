package com.softwaredevtools.standbot;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class RestStandbotService {

    @ComponentImport
    private final ActiveObjects ao;

    private final String STANDBOT_API_BASE_URL = "https://fhernandezngrok.ngrok.io/api/slack/";
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

    @GET
    @Path("slack/verify")
    public Response verifySlackInstallation(@QueryParam("subdomain") String subdomain) throws Exception {
        /*
        try to get the config for slack integration
         */
        SlackIntegrationEntity[] slackIntegrationEntities = ao.find(SlackIntegrationEntity.class);

        if (slackIntegrationEntities != null && slackIntegrationEntities.length > 0) {
            String result = searchForSlackSubdomain(subdomain, slackIntegrationEntities[0].getClientKey());

            if (result == null) {
                return Response.status(404).build();
            } else {
                return Response.ok(result).build();
            }
        } else {
            return Response.status(500).build();
        }
    }

    private String searchForSlackSubdomain(String subdomain, String clientKey) throws Exception {
        try {
            // create the HttpURLConnection
            URL url = new URL(STANDBOT_API_BASE_URL + "teams/search?subdomain=" + subdomain + "&clientKey=" + clientKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // just want to do an HTTP GET here
            connection.setRequestMethod("GET");

            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);
            connection.connect();

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
