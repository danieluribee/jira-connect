package com.softwaredevtools.standbot.service;

import com.softwaredevtools.standbot.service.StandbotAPI;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class StandbotAPITest {

    private StandbotAPI standbotAPI;

    @Before
    public void before() {
        standbotAPI = spy(new StandbotAPI(mock(JWTService.class)));
    }

    //TODO standbot api should be dynamic
    @Test
    @Ignore
    public void searchForSlackTeam() {
        standbotAPI.searchForSlackTeam("testdomain", "123");
        verify(standbotAPI).makeHttpCall(
                "http://localhost:3000/api/slack/teams/search?subdomain=testdomain&clientKey=123",
                "GET",
                null
        );
    }

    @Test
    @Ignore
    public void buildRelationSlackJira() {
        standbotAPI.buildRelationJiraSlack("11", "123", "localhost");
        verify(standbotAPI).makeHttpCall(
                "http://localhost:3000/api/jira-instances/current/relations?clientKey=123&hostBaseUrl=localhost",
                "POST",
                "{\"team_id\":\"11\"}"
        );
    }

}
