package com.softwaredevtools.standbot.model.pojo;

public class Fields {
    private String summary;
    private Priority priority;

    public Fields(String summary, String priorityName) {
        this.summary = summary;
        this.priority = new Priority(priorityName);
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSummary() {
        return summary;
    }
}
