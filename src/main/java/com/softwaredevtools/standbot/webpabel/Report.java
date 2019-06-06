package com.softwaredevtools.standbot.webpabel;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.softwaredevtools.standbot.config.StandbotConfig;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.service.JWTService;
import com.softwaredevtools.standbot.service.SlackIntegrationService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Report implements WebPanel {

    private final TemplateRenderer templateRenderer;
    private final SlackIntegrationService _slackIntegrationService;
    private ProjectManager _projectManager;
    private final JWTService _jwtService;


    public Report(TemplateRenderer renderer, SlackIntegrationService slackIntegrationService, JWTService jwtService) {
        _slackIntegrationService = slackIntegrationService;
        this.templateRenderer = renderer;
        _projectManager = ComponentAccessor.getProjectManager();
        _jwtService = jwtService;
    }

    public String getHtml(Map<String, Object> map) {
        /*
        try to get the config for slack integration or generate a new one if not exists
         */
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.generateSlackIntegrationIfNotExists();
        System.out.println("Using client key: " + slackIntegrationEntity.getClientKey());

        String projectKey = (String) map.getOrDefault("projectKey", null);
        String slackChannelId = "";

        try {
            slackChannelId = ((HttpServletRequest)map.get("request")).getParameter("slackChannelId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (projectKey == null) {
            return null;
        }

        Project project = _projectManager.getProjectObjByKey(projectKey);

        if (project == null) {
            return null;
        }

        String url = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("clientKey", slackIntegrationEntity.getClientKey());
        data.put("hostBaseUrl", url);
        String jwt = _jwtService.sign(data);

        map.put("slackChannelId", slackChannelId);
        map.put("projectId", project.getId());
        map.put("clientKey", slackIntegrationEntity.getClientKey());
        map.put("jwt", jwt);
        map.put("isLocal", StandbotConfig.ENVIRONMENT.equals(StandbotConfig.LOCAL));
        map.put("isStage", StandbotConfig.ENVIRONMENT.equals(StandbotConfig.STAGE));
        map.put("isProduction", StandbotConfig.ENVIRONMENT.equals(StandbotConfig.PRODUCTION));

        StringWriter stringWriter = new StringWriter();
        try {
            templateRenderer.render("templates/report.vm", map, stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stringWriter.toString();
    }

    public void writeHtml(Writer writer, Map<String, Object> map) throws IOException {
        writer.write(getHtml(map));
    }
}
