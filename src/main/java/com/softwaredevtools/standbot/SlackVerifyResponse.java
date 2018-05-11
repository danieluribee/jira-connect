package com.softwaredevtools.standbot;

public class SlackVerifyResponse {

    private String id;
    private boolean verified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
