package com.softwaredevtools.standbot.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.softwaredevtools.standbot.config.StandbotConfig;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.service.JWTService;
import com.softwaredevtools.standbot.service.SlackIntegrationService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Scanned
public class ReportServlet extends HttpServlet {

    @ComponentImport
    private final TemplateRenderer renderer;

    private final SlackIntegrationService _slackIntegrationService;
    private final JWTService _jwtService;
    private ProjectManager _projectManager;

    @Inject
    public ReportServlet(TemplateRenderer renderer, SlackIntegrationService slackIntegrationService, JWTService jwtService) {
        _slackIntegrationService = slackIntegrationService;
        this.renderer = renderer;
        _jwtService = jwtService;
        _projectManager = ComponentAccessor.getProjectManager();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
        try to get the config for slack integration or generate a new one if not exists
         */
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.generateSlackIntegrationIfNotExists();
        System.out.println("Using client key: " + slackIntegrationEntity.getClientKey());

        response.setContentType("text/html;charset=utf-8");

        String projectId = request.getParameter("project.id");
        String slackChannelId = request.getParameter("ac.slackChannelId");

        Project project = _projectManager.getProjectObj(Long.parseLong(projectId));

        if (project == null) {
            response.sendError(404, "Project not found");
        } else {
            String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
            response.sendRedirect(baseUrl + "/projects/" +  project.getKey() + "?selectedItem=slack-standup-bot-jira:standbotreport-project-render&slackChannelId=" + slackChannelId);
        }
    }

}
