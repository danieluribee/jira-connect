package com.softwaredevtools.standbot.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import java.util.UUID;

@Service
public class SlackIntegrationService {

    @ComponentImport
    private final ActiveObjects _ao;

    @ComponentImport
    private final JiraLicenseManager _jiraLicenseManager;

    @Inject
    public SlackIntegrationService(ActiveObjects ao, JiraLicenseManager jiraLicenseManager) {
        _jiraLicenseManager = jiraLicenseManager;
        _ao = ao;
    }

    public SlackIntegration getSlackIntegrationForHealthCheck() {
        SlackIntegration slackIntegration;

        SlackIntegrationEntity slackIntegrationEntity = getSlackIntegration();

        // if a record exists, then use its status
        if (slackIntegrationEntity != null) {
            slackIntegration = new SlackIntegration(slackIntegrationEntity.getActive(), slackIntegrationEntity.getClientKey());
        }
        //otherwise, use false
        else {
            slackIntegration = new SlackIntegration(false);
        }

        return slackIntegration;
    }

    public SlackIntegrationEntity getSlackIntegration() {
        SlackIntegrationEntity[] slackIntegrations = _ao.find(SlackIntegrationEntity.class);

        // if a record exists, then use its status
        if (slackIntegrations != null && slackIntegrations.length > 0) {
            return slackIntegrations[0];
        } else {
            return null;
        }
    }

    public SlackIntegrationEntity generateSlackIntegrationIfNotExists() {
        SlackIntegrationEntity slackIntegrationEntity = getSlackIntegration();

        if (slackIntegrationEntity == null) {
            System.out.println("Generate a new client key for the instance");

            UUID uuid = UUID.randomUUID();
            String generatedClientKey = uuid.toString();
            String serverId = _jiraLicenseManager.getServerId();

            slackIntegrationEntity = _ao.create(SlackIntegrationEntity.class);
            slackIntegrationEntity.setActive(false);
            slackIntegrationEntity.setClientKey(serverId + generatedClientKey);
            slackIntegrationEntity.save();
        }

        return slackIntegrationEntity;
    }

    public ApplicationUser getConfluenceUser() {
        return ComponentAccessor.getJiraAuthenticationContext().getUser();
    }
}
