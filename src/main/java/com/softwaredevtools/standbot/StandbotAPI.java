package com.softwaredevtools.standbot;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class StandbotAPI {

    //TODO standbot api should be dynamic
    private final String STANDBOT_API_BASE_URL = "http://localhost:3000/api/slack/";

    public String makeHttpCall(String requestUrl, String method) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);
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
        return makeHttpCall(STANDBOT_API_BASE_URL + "teams/search?subdomain=" + subdomain + "&clientKey=" + clientKey, "GET");
    }

}
