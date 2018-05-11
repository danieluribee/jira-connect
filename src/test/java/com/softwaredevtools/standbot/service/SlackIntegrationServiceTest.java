package com.softwaredevtools.standbot.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.license.JiraLicenseManager;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import com.softwaredevtools.standbot.service.SlackIntegrationService;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class SlackIntegrationServiceTest {

    private ActiveObjects ao;
    private JiraLicenseManager jiraLicenseManager;
    private SlackIntegrationService slackIntegrationService;

    @Before
    public void before() {
        ao = spy(ActiveObjects.class);
        jiraLicenseManager = spy(JiraLicenseManager.class);
        slackIntegrationService = new SlackIntegrationService(ao, jiraLicenseManager);
    }

    @Test
    public void getSlackIntegrationForHealthCheck_exists_activated() {
        /*
        prepare to return an instance with status activated
         */
        SlackIntegrationEntity slackIntegrationEntity = mock(SlackIntegrationEntity.class);
        doReturn(true).when(slackIntegrationEntity).getActive();
        doReturn(new SlackIntegrationEntity[]{slackIntegrationEntity}).when(ao).find(SlackIntegrationEntity.class);

        SlackIntegration slackIntegration = slackIntegrationService.getSlackIntegrationForHealthCheck();
        assertEquals(slackIntegration.isActive(), true);
    }

    @Test
    public void getSlackIntegrationForHealthCheck_exists_deactivated() {
        /*
        prepare to return an instance with status deactivated
         */
        SlackIntegrationEntity slackIntegrationEntity = mock(SlackIntegrationEntity.class);
        doReturn(false).when(slackIntegrationEntity).getActive();
        doReturn(new SlackIntegrationEntity[]{slackIntegrationEntity}).when(ao).find(SlackIntegrationEntity.class);

        SlackIntegration slackIntegration = slackIntegrationService.getSlackIntegrationForHealthCheck();
        assertEquals(slackIntegration.isActive(), false);
    }

    @Test
    public void getSlackIntegrationForHealthCheck_not_exists() {
        doReturn(null).when(ao).find(SlackIntegrationEntity.class);

        SlackIntegration slackIntegration = slackIntegrationService.getSlackIntegrationForHealthCheck();
        assertEquals(slackIntegration.isActive(), false);
    }

    @Test
    public void getSlackIntegration_null() {
        SlackIntegrationEntity slackIntegration = slackIntegrationService.getSlackIntegration();
        assertNull(slackIntegration);
    }

    @Test
    public void getSlackIntegration_not_null() {
        /*
        prepare to return an instance with status deactivated
         */
        SlackIntegrationEntity slackIntegrationEntity = mock(SlackIntegrationEntity.class);
        doReturn(false).when(slackIntegrationEntity).getActive();
        doReturn(new SlackIntegrationEntity[]{slackIntegrationEntity}).when(ao).find(SlackIntegrationEntity.class);

        SlackIntegration slackIntegration = slackIntegrationService.getSlackIntegrationForHealthCheck();
        assertNotNull(slackIntegration);
    }

    @Test
    public void generateSlackIntegrationIfNotExists_exists() {
        /*
        prepare to return an instance with status deactivated
         */
        SlackIntegrationEntity slackIntegrationEntity = mock(SlackIntegrationEntity.class);
        doReturn("123").when(slackIntegrationEntity).getClientKey();
        doReturn(new SlackIntegrationEntity[]{slackIntegrationEntity}).when(ao).find(SlackIntegrationEntity.class);

        SlackIntegrationEntity slackIntegration = slackIntegrationService.generateSlackIntegrationIfNotExists();
        assertEquals(slackIntegration.getClientKey(), "123");
    }

    @Test
    public void generateSlackIntegrationIfNotExists_not_exists() {
        doReturn(mock(SlackIntegrationEntity.class)).when(ao).create(SlackIntegrationEntity.class);

        SlackIntegrationEntity slackIntegration = slackIntegrationService.generateSlackIntegrationIfNotExists();
        verify(ao).create(SlackIntegrationEntity.class);
        verify(jiraLicenseManager).getServerId();
    }

}
