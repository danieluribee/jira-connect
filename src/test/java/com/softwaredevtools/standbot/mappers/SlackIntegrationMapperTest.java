package com.softwaredevtools.standbot.mappers;

import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.mappers.SlackIntegrationMapper;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SlackIntegrationMapperTest {

    @Test
    public void mapEntityToPojo() {
        SlackIntegrationEntity slackIntegrationEntity = spy(SlackIntegrationEntity.class);
        doReturn(true).when(slackIntegrationEntity).getActive();
        doReturn("123").when(slackIntegrationEntity).getClientKey();
        SlackIntegration slackIntegration = SlackIntegrationMapper.map(slackIntegrationEntity);

        verify(slackIntegrationEntity).getActive();
        verify(slackIntegrationEntity).getClientKey();

        assertEquals(slackIntegration.isActive(), slackIntegrationEntity.getActive());
        assertEquals(slackIntegration.getClientKey(), slackIntegrationEntity.getClientKey());
    }

}
