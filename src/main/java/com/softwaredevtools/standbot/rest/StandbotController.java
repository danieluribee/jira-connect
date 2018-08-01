package com.softwaredevtools.standbot.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.IssueList;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.softwaredevtools.standbot.service.JWTService;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;
import com.softwaredevtools.standbot.service.StandbotCustomAuthenticationService;
import org.ofbiz.core.entity.GenericEntityException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    private IssueManager _issueManager;
    private JWTService _jwtService;
    private SearchService _searchService;
    private StandbotCustomAuthenticationService _authenticationService;

    public StandbotController(SlackIntegrationService slackIntegrationService, StandbotAPI standbotAPI, JWTService jwtService,
                              StandbotCustomAuthenticationService authenticationService,
                              @ComponentImport SearchService searchService) {
        _authenticationService = authenticationService;
        _searchService = searchService;
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        _projectManager = ComponentAccessor.getProjectManager();
        _issueManager = ComponentAccessor.getIssueManager();
        _jwtService = jwtService;

        GSON = new Gson();
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

    @GET
    @AnonymousAllowed
    @Path("search")
    public Response search(@QueryParam("projectId") Long projectId, @QueryParam("userId") String userId,
                           @QueryParam("status") String status, @QueryParam("maxResults") String maxResults,
                           @QueryParam("jwt") @DefaultValue("") String jwt,
                           @QueryParam("registeredJiraUrl") String registeredJiraUrl) throws GenericEntityException, SearchException {
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

        ApplicationUser user = ComponentAccessor.getUserManager().getUser(userId);

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

        List<Issue> issues = _searchService.search(user, parseResult.getQuery(), new PagerFilter(5)).getIssues();

        IssueList issueList = new IssueList();
        issueList.setIssues(issues, registeredJiraUrl);

        return Response.ok(GSON.toJson(issueList, IssueList.class)).build();
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
}
