package com.softwaredevtools.standbot.model.pojo;

import com.atlassian.jira.issue.Issue;

import java.util.ArrayList;
import java.util.List;

public class IssueList {
    private List<CustomIssue> issues;

    public List<CustomIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues, String registeredJiraUrl) {
        this.issues = new ArrayList<CustomIssue>(issues.size());

        for (Issue originalIssue : issues) {
            this.issues.add(new CustomIssue(originalIssue, registeredJiraUrl));
        }
    }
}
