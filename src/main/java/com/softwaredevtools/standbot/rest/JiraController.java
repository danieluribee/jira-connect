package com.softwaredevtools.standbot.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.mappers.SlackIntegrationMapper;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.softwaredevtools.standbot.service.JWTService;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;
import com.softwaredevtools.standbot.service.StandbotCustomAuthenticationService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class JiraController {
    private final SlackIntegrationService _slackIntegrationService;
    private final StandbotAPI _standbotAPI;
    private Gson GSON;
    private ProjectManager _projectManager;

    public JiraController(SlackIntegrationService slackIntegrationService, StandbotAPI standbotAPI) {
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        _projectManager = ComponentAccessor.getProjectManager();
        GSON = new Gson();
    }

    @GET
    @Path("jira/projects")
    public Response getProjects(@QueryParam("hostBaseUrl") String hostBaseUrl) {
        List<Project> projects = _projectManager.getProjectObjects();
        return Response.ok(GSON.toJson(projects)).build();
    }

    @POST
    @Path("jira/projects/{projectId}/standups")
    public Response saveStandupRelation(@QueryParam("hostBaseUrl") String hostBaseUrl, @PathParam("projectId") String projectId,
                                        @QueryParam("channelId") String slackChannelId, @QueryParam("teamId") String slackTeamId) {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        String response = _standbotAPI.saveRelationChannelProject(slackIntegration.getClientKey(), hostBaseUrl, projectId, slackChannelId, slackTeamId);
        return Response.ok(response).build();
    }
}
