package com.softwaredevtools.standbot.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.softwaredevtools.standbot.config.StandbotConfig;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.service.SlackIntegrationService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Scanned
public class UserRelationServlet extends HttpServlet {

    @ComponentImport
    private final TemplateRenderer renderer;

    private final SlackIntegrationService _slackIntegrationService;

    @Inject
    public UserRelationServlet(TemplateRenderer renderer, SlackIntegrationService slackIntegrationService) {
        _slackIntegrationService = slackIntegrationService;
        this.renderer = renderer;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
        try to get the config for slack integration or generate a new one if not exists
         */
        SlackIntegrationEntity slackIntegrationEntity = _slackIntegrationService.generateSlackIntegrationIfNotExists();
        System.out.println("Using client key: " + slackIntegrationEntity.getClientKey());

        response.setContentType("text/html;charset=utf-8");

        HashMap<String, Object> map = new HashMap();

        map.put("clientKey", slackIntegrationEntity.getClientKey());
        map.put("isLocal", StandbotConfig.ENVIRONMENT.equals(StandbotConfig.LOCAL));

        String slackUserId = request.getParameter("slackUserId");
        String slackTeamId = request.getParameter("slackTeamId");

        if (slackUserId != null && slackTeamId != null && !slackUserId.isEmpty() && !slackTeamId.isEmpty()) {
            map.put("slackUserId", slackUserId);
            map.put("slackTeamId", slackTeamId);
            renderer.render("templates/user-relation.vm", map, response.getWriter());
        } else {
            response.sendError(400, "slackUserId and slackTeamId paramaeters are required");
        }
    }
}
