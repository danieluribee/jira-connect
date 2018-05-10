package com.softwaredevtools.standbot.model;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("STANDBOT_STATUS")
public interface SlackIntegrationEntity extends Entity {
    boolean getActive();

    void setActive(boolean active);

    String getClientKey();

    void setClientKey(String clientKey);
}
