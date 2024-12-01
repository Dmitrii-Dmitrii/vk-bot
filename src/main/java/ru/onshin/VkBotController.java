package ru.onshin;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class VkBotController {
    @Value("${vk.confirmation.code}")
    private String confirmationCode;

    @Value("${vk.access.token}")
    private String accessToken;

    @PostMapping
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");

        if ("confirmation".equals(type)) {
            // Возвращаем код подтверждения, чтобы VK активировал Callback API
            return ResponseEntity.ok(confirmationCode);
        }

        if ("message_new".equals(type)) {
            Map<String, Object> object = (Map<String, Object>) request.get("object");
            Map<String, Object> message = (Map<String, Object>) object.get("message");

            String userId = String.valueOf(message.get("from_id"));
            String text = (String) message.get("text");

            // Отправляем сообщение
            sendMessage(userId, text);
        }

        return ResponseEntity.ok("ok");
    }

    private void sendMessage(String userId, String text) {
        try {
            String apiUrl = "https://api.vk.com/method/messages.send";
            String params = String.format(
                    "?user_id=%s&random_id=%d&message=%s&access_token=%s&v=5.131",
                    userId, System.currentTimeMillis(), text, accessToken
            );

            new java.net.URL(apiUrl + params).openStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
