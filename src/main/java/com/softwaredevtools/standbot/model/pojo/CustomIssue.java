package com.softwaredevtools.standbot.model.pojo;

import com.atlassian.jira.issue.Issue;

public class CustomIssue {
    private String key;
    private Long id;
    private String status;
    private String self;
    private Fields fields;

    public CustomIssue(Issue issue, String registeredJiraUrl) {
        key = issue.getKey();
        id = issue.getId();
        status = issue.getStatus().getSimpleStatus().getName();
        self = registeredJiraUrl + "/rest/api/2/issue/" + id;
        fields = new Fields(issue.getSummary(), issue.getPriority().getName());
    }

    public String getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }
}
