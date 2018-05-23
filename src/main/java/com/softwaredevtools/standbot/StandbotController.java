package com.softwaredevtools.standbot;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.gson.reflect.TypeToken;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.mappers.SlackIntegrationMapper;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;
import org.ofbiz.core.entity.GenericEntityException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Type;
import java.util.*;

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

    public StandbotController(SlackIntegrationService slackIntegrationService, StandbotAPI standbotAPI) {
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        GSON = new Gson();
        _projectManager = ComponentAccessor.getProjectManager();
        _issueManager = ComponentAccessor.getIssueManager();
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
                           @QueryParam("registeredJiraUrl") String registeredJiraUrl) throws GenericEntityException {
        //option: done, in-progress, assigned
        Collection<Long> issueIds = _issueManager.getIssueIdsForProject(projectId);
        List<MutableIssue> issues = new ArrayList<MutableIssue>();

        for (Long id : issueIds) {
            MutableIssue issue = _issueManager.getIssueObject(id);

            if (!issue.getAssigneeId().equals(userId)) {
                continue;
            }

            if (status.equals("done")) {
                if (!issue.getStatus().getSimpleStatus().getName().equals("Done")) {
                    continue;
                }
            } else if (status.equals("in-progress")) {
                if (!issue.getStatus().getSimpleStatus().getName().equals("In Progress")) {
                    continue;
                }
            } else if (status.equals("assigned")) {
                if (!issue.getStatus().getSimpleStatus().getName().equals("Done") && !issue.getStatus().getSimpleStatus().getName().equals("In Progress")) {
                    continue;
                }
            }

            issues.add(issue);
        }

        Collections.sort(issues, new Comparator<MutableIssue>() {
            public int compare(MutableIssue o1, MutableIssue o2) {
                return o2.getUpdated().compareTo(o2.getUpdated());
            }
        });

        List<MutableIssue> finalList = issues.subList(0, issues.size() >= 5 ? 5 : issues.size());
        IssueList issueList = new IssueList();
        issueList.setIssues(finalList, registeredJiraUrl);

        return Response.ok(GSON.toJson(issueList, IssueList.class)).build();

    }

    private class Priority {
        private String name;

        public Priority(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private class Fields {
        private String summary;
        private Priority priority;

        public Fields(String summary, String priorityName) {
            this.summary = summary;
            this.priority = new Priority(priorityName);
        }

        public Priority getPriority() {
            return priority;
        }

        public String getSummary() {
            return summary;
        }
    }

    private class Issue {
        private String key;
        private Long id;
        private String status;
        private String self;
        private Fields fields;

        public Issue(MutableIssue issue, String registeredJiraUrl) {
            key = issue.getKey();
            id = issue.getId();
            status = issue.getStatus().getSimpleStatus().getName();
            self = registeredJiraUrl + "/rest/api/2/issue/" + id;
            fields = new Fields(issue.getSummary(), issue.getPriority().getName());
        }

        public String getStatus() {
            return status;
        }

        public Long getId() {
            return id;
        }

        public String getKey() {
            return key;
        }
    }

    private class IssueList {
        private List<Issue> issues;

        public List<Issue> getIssues() {
            return issues;
        }

        public void setIssues(List<MutableIssue> issues, String registeredJiraUrl) {
            this.issues = new ArrayList<Issue>(issues.size());

            for (MutableIssue originalIssue : issues) {
                this.issues.add(new Issue(originalIssue, registeredJiraUrl));
            }
        }
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
