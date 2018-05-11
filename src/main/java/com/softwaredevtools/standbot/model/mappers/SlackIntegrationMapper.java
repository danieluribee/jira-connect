package com.softwaredevtools.standbot.model.mappers;

import com.softwaredevtools.standbot.model.SlackIntegrationEntity;
import com.softwaredevtools.standbot.model.pojo.SlackIntegration;

public class SlackIntegrationMapper {

    public static SlackIntegration map(SlackIntegrationEntity slackIntegrationEntity) {
        return new SlackIntegration(
                slackIntegrationEntity.getActive(),
                slackIntegrationEntity.getClientKey()
        );
    }

}
