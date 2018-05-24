package com.softwaredevtools.standbot.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class StandbotAPI {

    //TODO standbot api should be dynamic
    private final String STANDBOT_API_BASE_URL = "http://localhost:3000/api/";

    private Gson GSON;

    public StandbotAPI() {
        this.GSON = new Gson();
    }

    public String makeHttpCall(String requestUrl, String method, String json) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);

            if (method.equals("POST")) {
                connection.setDoOutput(true);
                connection.addRequestProperty("Content-type", "application/json");
                connection.getOutputStream().write(json.getBytes("UTF-8"));
            }

            connection.connect();

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String searchForSlackTeam(String subdomain, String clientKey) {
        return makeHttpCall(STANDBOT_API_BASE_URL + "slack/teams/search?subdomain=" + subdomain + "&clientKey=" + clientKey, "GET", null);
    }

    public String buildRelationJiraSlack(String teamId, String clientKey, String hostBaseUrl) {
        return makeHttpCall(
                STANDBOT_API_BASE_URL + "jira-instances/current/relations?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl,
                "POST",
                "{\"team_id\":\"" + teamId + "\"}"
        );
    }

    public String getSlackRelations(String clientKey, String hostBaseUrl) {
        return makeHttpCall(STANDBOT_API_BASE_URL + "jira-instances/current/relations?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl,
                "GET",
                null
        );
    }

    public String verifyRelation(String clientKey, String userId, String hostBaseUrl, String slackUserId, String slackTeamId) {
        return makeHttpCall(STANDBOT_API_BASE_URL + "jira-instances/current/relations/verify?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl + "&slackUserId=" + slackUserId + "&slackTeamId=" + slackTeamId + "&userId=" + userId,
                "GET",
                null
        );
    }

    public String getSlackTeams(String clientKey, String hostBaseUrl, String slackTeamId) {
        return makeHttpCall(
                STANDBOT_API_BASE_URL + "slack/teams/" + slackTeamId + "?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl,
                "GET",
                null
        );
    }

    public String getStandups(String clientKey, String hostBaseUrl, String teamId) {
        return makeHttpCall(
                STANDBOT_API_BASE_URL + "slack/teams/" + teamId + "/standups?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl,
                "GET",
                null
        );
    }

    public String saveRelationChannelProject(String clientKey, String hostBaseUrl, String projectId, String slackChannelId, String slackTeamId) {
        return makeHttpCall(
                STANDBOT_API_BASE_URL + "jira/projects/" + projectId + "/standups?clientKey=" + clientKey + "&hostBaseUrl=" + hostBaseUrl,
                "POST",
                "{\"slack_channel_id\":\"" + slackChannelId + "\", \"slack_team_id\":\"" + slackTeamId + "\"}"
        );
    }
}