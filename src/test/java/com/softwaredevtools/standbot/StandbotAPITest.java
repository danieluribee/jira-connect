package com.softwaredevtools.standbot;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import net.java.ao.RawEntity;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class StandbotAPITest {

    private StandbotAPI standbotAPI;

    @Before
    public void before() {
        standbotAPI = spy(new StandbotAPI());
    }

    //TODO standbot api should be dynamic
    @Test
    public void searchForSlackTeam() {
        standbotAPI.searchForSlackTeam("testdomain", "123");
        verify(standbotAPI).makeHttpCall(
                "http://localhost:3000/api/slack/teams/search?subdomain=testdomain&clientKey=123",
                "GET"
        );
    }

}
