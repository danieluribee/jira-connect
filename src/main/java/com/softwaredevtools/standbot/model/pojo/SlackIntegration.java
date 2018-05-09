package com.softwaredevtools.standbot.model.pojo;

public class SlackIntegration {

    private boolean active;

    public SlackIntegration(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
