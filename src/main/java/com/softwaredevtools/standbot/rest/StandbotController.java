package com.softwaredevtools.standbot.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.SlackVerifyResponse;
import com.softwaredevtools.standbot.config.StandbotConfig;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.mappers.SlackIntegrationMapper;
import com.softwaredevtools.standbot.model.pojo.*;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;
import com.softwaredevtools.standbot.service.StandbotCustomAuthenticationService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class StandbotController {
    private final StandbotAPI _standbotAPI;
    private ProjectManager _projectManager;
    private final SlackIntegrationService _slackIntegrationService;
    private Gson GSON;

    @ComponentImport
    private final PluginLicenseManager _pluginLicenseManager;

    @ComponentImport
    private SearchService _searchService;
    private StandbotCustomAuthenticationService _authenticationService;

    public StandbotController(SlackIntegrationService slackIntegrationService,
                              StandbotCustomAuthenticationService authenticationService,
                              SearchService searchService, StandbotAPI standbotAPI,
                              PluginLicenseManager pluginLicenseManager) {
        _authenticationService = authenticationService;
        _searchService = searchService;
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        _projectManager = ComponentAccessor.getProjectManager();
        _pluginLicenseManager = pluginLicenseManager;

        GSON = new Gson();
    }

    /*
        will indicate if the plugin is installed
        will be used by the bot server to know if the add-on is up and running on the Jira server
     */
    @GET
    @AnonymousAllowed
    @Path("healthcheck")
    public Response healthcheck(@QueryParam("clientKey") @DefaultValue("") String clientKey,
                                @QueryParam("jwt") @DefaultValue("") String jwt) {
        SlackIntegration slackIntegration = _slackIntegrationService.getSlackIntegrationForHealthCheck();

        /*
        the standbot server should send the right clientKey
         */
        if (!slackIntegration.getClientKey().equals(clientKey)) {
            return Response.status(403).build();
        }

        /*
        validate that the standbot server is using the same JWT_SECRET
         */
        if (!_authenticationService.isValid(jwt)) {
            return Response.status(403).build();
        }

        if (StandbotConfig.ENVIRONMENT.equals("LOCAL") || StandbotConfig.ENVIRONMENT.equals("STAGE")) {
            return Response.ok().build();
        } else {
            if(_pluginLicenseManager.getLicense().isDefined() && !_pluginLicenseManager.getLicense().get().getError().isDefined()) {
                return Response.ok().build();
            } else {
                return Response.status(403).build();
            }
        }
    }

    @GET
    @AnonymousAllowed
    @Path("search")
    public Response search(@QueryParam("projectId") Long projectId, @QueryParam("userId") String userId,
                           @QueryParam("status") String status, @QueryParam("maxResults") String maxResults,
                           @QueryParam("standbot-jwt") @DefaultValue("") String jwt,
                           @QueryParam("registeredJiraUrl") String registeredJiraUrl) throws Exception {
        if (jwt.isEmpty()) {
            //bad request since jwt is required
            return Response.status(400).build();
        }

        if(!_authenticationService.isValid(jwt)) {
            //forbidden, since couldn't decode the jwt and it's supposed to be signed with the same SECRET
            return Response.status(403).build();
        }

        SlackIntegrationEntity entity = _slackIntegrationService.generateSlackIntegrationIfNotExists();

        if(!entity.getActive()) {
            // the integration is deactivated in the server
            return Response.status(403).build();
        }

        ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userId);

        String jqlStatus = "";

        if (status.equals("done")) {
            jqlStatus = "statusCategory = \"Done\"";
        } else if (status.equals("in-progress")) {
            jqlStatus = "statusCategory = \"In Progress\"";
        } else if (status.equals("assigned")) {
            jqlStatus = "statusCategory != \"Done\" and statusCategory != \"In Progress\"";
        }

        String jql = "project = " + projectId + " and " + jqlStatus + " and assignee = currentUser() ORDER BY updated DESC";

        final SearchService.ParseResult parseResult = _searchService.parseQuery(user, jql);

        if (!parseResult.isValid()) {
            throw new IllegalArgumentException("Invalid JQL query specified for chart '" + jql + "'.");
        }

        SearchResults searchResults = _searchService.search(user, parseResult.getQuery(), new PagerFilter(5));

        Method method = null;
        try {
            method = searchResults.getClass().getMethod("getIssues");
            System.out.println("Will try to use getIssues to list issues");
        } catch (NoSuchMethodException e) {
            System.out.println("getIssues method doesn't exist");
        }

        if (method == null) {
            try {
                method = searchResults.getClass().getMethod("getResults");
                System.out.println("Will try to use getResults to list issues");
            } catch (NoSuchMethodException e) {
                System.out.println("getResults method doesn't exist");
            }
        }

        if (method == null) {
            return Response.status(500).entity("Can't locate proper method to get issues").build();
        }

        try {
            System.out.println("Try to list issues using " + method.getName());
            List<Issue> issues = (List<Issue>) method.invoke(searchResults);
            IssueList issueList = new IssueList();
            issueList.setIssues(issues, registeredJiraUrl);

            return Response.ok(GSON.toJson(issueList, IssueList.class)).build();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return Response.status(500).entity("Error in issues result invocation").build();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return Response.status(500).entity("Error in issues result invocation").build();
        }
    }

    @POST
    @Path("activate")
    public Response activateIntegration() {
        SlackIntegrationEntity entity = _slackIntegrationService.generateSlackIntegrationIfNotExists();

        entity.setActive(true);
        entity.save();

        return Response.ok().build();
    }

    @POST
    @Path("deactivate")
    public Response deactivate() {
        SlackIntegrationEntity entity = _slackIntegrationService.generateSlackIntegrationIfNotExists();

        entity.setActive(false);
        entity.save();

        return Response.ok().build();
    }


    @GET
    @Path("jira/projects")
    public Response getProjects(@QueryParam("hostBaseUrl") String hostBaseUrl) {
        List<Project> projects = _projectManager.getProjectObjects();
        List<ProjectDTO> projectDTOs = new LinkedList<ProjectDTO>();

        for (Project project : projects) {
            projectDTOs.add(new ProjectDTO(project.getId(), project.getName()));
        }

        return Response.ok(GSON.toJson(projectDTOs)).build();
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

    @POST
    @Path("jira/relations")
    public Response saveStandupRelations(@QueryParam("hostBaseUrl") String hostBaseUrl,
                                         RelationsPayload payload) {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        String response = _standbotAPI.saveRelations(slackIntegration.getClientKey(), hostBaseUrl, payload);

        return Response.ok(response).build();
    }

    @POST
    @Path("/jira/notifyJiraInStandup")
    public Response notifyJiraInStandup(@QueryParam("hosttBaseUrl") String hostBaseUrl,
                                        NotifyPayload notifyPaylod) {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        String response = _standbotAPI.notifyJiraInStandup(slackIntegration.getClientKey(), hostBaseUrl, notifyPaylod);

        return Response.ok(response).build();
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
        } else if (result == "HTTP Response code: 409") {
            return Response.status(409).build();
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
    @Path("slack/configuration")
    public Response getSlackConfiguration() {
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);
        return Response.ok(GSON.toJson(slackIntegration)).build();
    }

    @PUT
    @Path("jira-addon/configurations/{Id}")
    public Response updateConfigSettings(@PathParam("Id") String Id, @QueryParam("hostBaseUrl") String hostBaseUrl, String payload){
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.getSlackIntegration();

        if (slackIntegrationEntity == null) {
            return Response.status(404).build();
        }

        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        Object response = _standbotAPI.updateConfigSettings(slackIntegration.getClientKey(), hostBaseUrl, Id, payload);
        return Response.ok(response).build();
    }
}
