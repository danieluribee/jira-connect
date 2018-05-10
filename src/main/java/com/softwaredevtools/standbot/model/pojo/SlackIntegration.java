package com.softwaredevtools.standbot.model.pojo;

public class SlackIntegration {

    private boolean active;
    private String clientKey;

    public SlackIntegration(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
}
