package com.softwaredevtools.standbot.model.pojo;

public class Standup {
    private String jira_project_id;
    private String platform_conversation_id;
    private String name;
    private String team_id;

    public String getJira_project_id() {
        return jira_project_id;
    }

    public String getPlatform_conversation_id() {
        return platform_conversation_id;
    }

    public void setJira_project_id(String jira_project_id) {
        this.jira_project_id = jira_project_id;
    }

    public void setPlatform_conversation_id(String platform_conversation_id) {
        this.platform_conversation_id = platform_conversation_id;
    }

    public String getTeam_id() {
        return team_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }
}
