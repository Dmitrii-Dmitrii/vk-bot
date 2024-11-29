package ru.onshin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.onshin.service.VkBotService;

@Component
public class VkLongPollHandler {

    @Autowired
    private VkBotService vkBotService;

    public void start() throws Exception {
        JsonObject serverInfo = vkBotService.getLongPollServer();
        String server = serverInfo.get("server").getAsString();
        String key = serverInfo.get("key").getAsString();
        String ts = serverInfo.get("ts").getAsString();

        while (true) {
            String url = server + "?act=a_check&key=" + key + "&ts=" + ts + "&wait=25";
            String response = Request.get(url).execute().returnContent().asString();
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("failed")) {
                serverInfo = vkBotService.getLongPollServer();
                server = serverInfo.get("server").getAsString();
                key = serverInfo.get("key").getAsString();
                ts = serverInfo.get("ts").getAsString();
            } else {
                ts = jsonResponse.get("ts").getAsString();
                processEvents(jsonResponse.get("updates").getAsJsonArray());
            }
        }
    }

    private void processEvents(JsonArray updates) throws Exception {
        for (JsonElement update : updates) {
            JsonObject updateObj = update.getAsJsonObject();
            if ("message_new".equals(updateObj.get("type").getAsString())) {
                JsonObject message = updateObj.get("object").getAsJsonObject().get("message").getAsJsonObject();
                String userId = message.get("from_id").getAsString();
                String text = message.get("text").getAsString();

                System.out.println("Новое сообщение от " + userId + ": " + text);
                vkBotService.sendMessage(userId, "Вы написали: " + text);
            }
        }
    }
}