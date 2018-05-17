package com.softwaredevtools.standbot;

public class SlackRelationVerificationResponse {
    private boolean wasVerified;
    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setWasVerified(boolean wasVerified) {
        this.wasVerified = wasVerified;
    }

    public boolean isWasVerified() {
        return wasVerified;
    }
}