package ru.onshin.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VkBotService {

    @Value("${access-token}")
    private String accessToken;

    @Value("${version}")
    private String apiVersion;

    @Value("${group-id}")
    private String groupId;

    public JsonObject getLongPollServer() throws Exception {
        String url = "https://api.vk.com/method/groups.getLongPollServer?group_id=" + groupId +
                "&access_token=" + accessToken +
                "&v=" + apiVersion;
        String response = Request.get(url).execute().returnContent().asString();
        return JsonParser.parseString(response).getAsJsonObject().get("response").getAsJsonObject();
    }

    public void sendMessage(String userId, String message) throws Exception {
        String url = "https://api.vk.com/method/messages.send?" +
                "user_id=" + userId +
                "&random_id=" + System.currentTimeMillis() +
                "&message=" + message +
                "&access_token=" + accessToken +
                "&v=" + apiVersion;

        Request.get(url).execute();
    }
}
