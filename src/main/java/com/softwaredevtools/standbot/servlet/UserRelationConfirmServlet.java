package com.softwaredevtools.standbot.servlet;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;
import com.softwaredevtools.standbot.SlackRelationVerificationResponse;
import com.softwaredevtools.standbot.config.StandbotConfig;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import com.softwaredevtools.standbot.service.StandbotAPI;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Scanned
public class UserRelationConfirmServlet extends HttpServlet {

    @ComponentImport
    private final TemplateRenderer renderer;

    private final SlackIntegrationService _slackIntegrationService;
    private final StandbotAPI _standbotAPI;
    private final Gson GSON;

    @Inject
    public UserRelationConfirmServlet(TemplateRenderer renderer, SlackIntegrationService slackIntegrationService, StandbotAPI standbotAPI) {
        _slackIntegrationService = slackIntegrationService;
        _standbotAPI = standbotAPI;
        this.renderer = renderer;
        GSON = new Gson();
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

        String slackUserId = request.getParameter("slackUserId");
        String slackTeamId = request.getParameter("slackTeamId");

        if (slackUserId != null && slackTeamId != null && !slackUserId.isEmpty() && !slackTeamId.isEmpty()) {
            map.put("slackUserId", slackUserId);
            map.put("slackTeamId", slackTeamId);
            map.put("isLocal", StandbotConfig.ENVIRONMENT.equals(StandbotConfig.LOCAL));

            try {
                ApplicationUser user = _slackIntegrationService.getConfluenceUser();
                String userKey = user.getKey();
                String standbotResponse = _standbotAPI.verifyRelation(slackIntegrationEntity.getClientKey(), userKey, request.getLocalName(), slackUserId, slackTeamId);
                SlackRelationVerificationResponse slackRelationVerificationResponse = GSON.fromJson(standbotResponse, SlackRelationVerificationResponse.class);

                if (slackRelationVerificationResponse.isVerified()) {
                    slackIntegrationEntity.setActive(true);
                    slackIntegrationEntity.save();
                }

                map.put("standbotResponse", slackRelationVerificationResponse);
                renderer.render("templates/user-relation-confirm.vm", map, response.getWriter());
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            }
        } else {
            response.sendError(400, "slackUserId and slackTeamId paramaeters are required");
        }
    }

}
