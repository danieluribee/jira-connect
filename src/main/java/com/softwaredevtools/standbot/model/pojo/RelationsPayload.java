package com.softwaredevtools.standbot.model.pojo;

public class RelationsPayload {
    private String slack_team_id;
    private Relation[] relations;

    public String getSlack_team_id() {
        return slack_team_id;
    }

    public void setSlack_team_id(String slack_team_id) {
        this.slack_team_id = slack_team_id;
    }

    public Relation[] getRelations() {
        return relations;
    }

    public void setRelations(Relation[] relations) {
        this.relations = relations;
    }
}
