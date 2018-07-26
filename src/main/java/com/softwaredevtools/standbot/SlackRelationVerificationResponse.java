package com.softwaredevtools.standbot;

public class SlackRelationVerificationResponse {
    private boolean verified;
    private Messaging messaging;

    public Messaging getMessaging() {
        return messaging;
    }

    public void setMessaging(Messaging messaging) {
        this.messaging = messaging;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }
}