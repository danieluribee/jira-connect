package com.softwaredevtools.standbot.servlet;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;

@Scanned
public class ConfigureServlet extends HttpServlet {

    @ComponentImport
    private final TemplateRenderer renderer;

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ConfigureServlet(TemplateRenderer renderer, ActiveObjects ao) {
        this.ao = ao;
        this.renderer = renderer;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        /*
        try to get the config for slack integration
         */
        SlackIntegrationEntity[] slackIntegrationEntities = ao.find(SlackIntegrationEntity.class);

        /*
        this is the first time we see this screen
        we'll generate a new unique id for the jira instance
         */
        if (slackIntegrationEntities == null || slackIntegrationEntities.length == 0) {
            SlackIntegrationEntity slackIntegrationEntity = ao.create(SlackIntegrationEntity.class);
            slackIntegrationEntity.setActive(false);

            UUID uuid = UUID.randomUUID();
            String generatedClientKey = uuid.toString();

            slackIntegrationEntity.setClientKey(generatedClientKey);
            slackIntegrationEntity.save();
        } else {
            System.out.println("Using client key: " + slackIntegrationEntities[0].getClientKey());
        }

        response.setContentType("text/html;charset=utf-8");
        renderer.render("templates/admin.vm", response.getWriter());
    }

}
