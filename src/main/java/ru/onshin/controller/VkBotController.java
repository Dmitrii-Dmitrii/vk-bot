package ru.onshin.controller;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
public class VkBotController {
    @Value("${vk.confirmation.code}")
    private String confirmationCode;

    @Value("${vk.access.token}")
    private String accessToken;

    private int responseCode;

    private int lastProcessedTimestamp = 0;

    private Set<String> processedEventIds = new HashSet<>();

    @PostMapping
    public ResponseEntity<?> handleMessage(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String eventId = (String) request.get("event_id");

        log.info("Received callback request of type: {}", type);

        if ("message_reply".equals(type)) {
            log.info("Message already processed, skipping.");
            return ResponseEntity.status(responseCode).body(HttpStatus.OK);
        }

        if ("confirmation".equals(type)) {
            log.info("Returning confirmation code.");
            return ResponseEntity.ok(confirmationCode);
        }

        if ("message_new".equals(type)) {
            Map<String, Object> object = (Map<String, Object>) request.get("object");
            Map<String, Object> message = (Map<String, Object>) object.get("message");
            String userId = String.valueOf(message.get("from_id"));

            if (processedEventIds.contains(eventId)) {
                log.info("Event {} already processed, skipping.", eventId);
                log.info("Message text: {}", message.get("text"));
                return ResponseEntity.status(responseCode).body(HttpStatus.OK);
            }

            int messageTimestamp = (Integer) message.get("date");

            if (messageTimestamp <= lastProcessedTimestamp) {
                log.info("Skipping message, it is older than the last processed message.");
                return ResponseEntity.ok(HttpStatus.OK);
            }

            lastProcessedTimestamp = messageTimestamp;

            log.debug("Processing new message event: {}", request);
            processedEventIds.add(eventId);

            String text = "Вы сказали: " + message.get("text");

            log.info("Sending message to user: {} with text: {}", userId, text);
            sendMessage(userId, text);
        }

        return responseCode == 200
                ? ResponseEntity.status(responseCode).body(HttpStatus.OK)
                : ResponseEntity.status(responseCode).body(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void sendMessage(String userId, String text) {
        try {
            String apiUrl = "https://api.vk.com/method/messages.send";
            String encodedMessage = URLEncoder.encode(text, StandardCharsets.UTF_8);

            String params = String.format(
                    "?user_id=%s&random_id=%d&message=%s&access_token=%s&v=5.131",
                    userId, System.currentTimeMillis(), encodedMessage, accessToken
            );

            log.debug("Constructed VK API request URL: {}", apiUrl + params);

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl + params).openConnection();
            connection.setRequestMethod("GET");

            responseCode = connection.getResponseCode();

            log.info("VK API response code: {}", responseCode);
        } catch (Exception e) {
            log.error("Error occurred while sending message: {}", e.getMessage(), e);
        }
    }
}
