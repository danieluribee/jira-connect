package com.softwaredevtools.standbot;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.mappers.SlackIntegrationMapper;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class StandbotController {
    private final SlackIntegrationService _slackIntegrationService;
    private final StandbotAPI _standbotAPI;
    private Gson GSON;
    private ProjectManager _projectManager;

    public StandbotController(SlackIntegrationService slackIntegrationService, StandbotAPI standbotAPI) {
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        GSON = new Gson();
        _projectManager = ComponentAccessor.getProjectManager();
    }

    /*
        will indicate if the plugin is installed
        will be used by the bot server to know if the add-on is up and running on the Jira server
     */
    @GET
    @AnonymousAllowed
    @Path("healthcheck")
    public Response healthcheck() {
        SlackIntegration slackIntegration = _slackIntegrationService.getSlackIntegrationForHealthCheck();
        return Response.ok(GSON.toJson(slackIntegration)).build();
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
    @Path("slack/relations")
    public Response getSlackRelations(@QueryParam("hostBaseUrl") String hostBaseUrl) {
        /*
        try to get the config for slack integration
         */
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(500).build();
        }

        String clientKey = slackIntegrationEntity.getClientKey();
        String result = _standbotAPI.getSlackRelations(clientKey, hostBaseUrl);

        return Response.ok(result).build();
    }

    @GET
    @Path("slack/verify")
    public Response verifySlackInstallation(@QueryParam("subdomain") String subdomain, @QueryParam("hostBaseUrl") String hostBaseUrl) throws Exception {
        /*
        try to get the config for slack integration
         */
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(500).build();
        }

        String clientKey = slackIntegrationEntity.getClientKey();
        String result = _standbotAPI.searchForSlackTeam(subdomain, clientKey);

        if (result == null) {
            return Response.status(404).build();
        } else {
            SlackVerifyResponse slackVerifyResponse = GSON.fromJson(result, SlackVerifyResponse.class);

            String teamId = slackVerifyResponse.getId();
            String resultRelation = _standbotAPI.buildRelationJiraSlack(teamId, clientKey, hostBaseUrl);
            return Response.ok(resultRelation).build();
        }
    }

    @GET
    @Path("slack/teams/{teamId}")
    public Response getSlackTeams(@PathParam("teamId") String teamId, @QueryParam("hostBaseUrl") String hostBaseUrl) {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        String response = _standbotAPI.getSlackTeams(slackIntegration.getClientKey(), hostBaseUrl, teamId);
        return Response.ok(response).build();
    }

    @GET
    @Path("slack/teams/{teamId}/standups")
    public Response getStandups(@PathParam("teamId") String teamId, @QueryParam("hostBaseUrl") String hostBaseUrl) {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        String response = _standbotAPI.getStandups(slackIntegration.getClientKey(), hostBaseUrl, teamId);
        return Response.ok(response).build();
    }

    @GET
    @Path("jira/projects")
    public Response getProjects(@QueryParam("hostBaseUrl") String hostBaseUrl) {
        List<Project> projects = _projectManager.getProjectObjects();
        return Response.ok(GSON.toJson(projects)).build();
    }


    @GET
    @Path("slack/configuration")
    public Response getSlackConfiguration() {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);
        return Response.ok(GSON.toJson(slackIntegration)).build();
    }
}
