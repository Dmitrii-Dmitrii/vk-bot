package ru.onshin.controller;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Контроллер для обработки входящих сообщений от ВКонтакте и отправки ответов пользователям.
 * Обрабатывает подтверждение, а также новые сообщения, поступающие от ВКонтакте.
 */
@Controller
@Slf4j
public class VkBotController {

    @Value("${vk.confirmation.code}")
    private String confirmationCode;

    @Value("${vk.access.token}")
    private String accessToken;

    private int lastProcessedTimestamp = 0;

    /**
     * Обрабатывает входящие callback-запросы от ВКонтакте.
     * В зависимости от типа запроса (confirmation или message_new), вызывает соответствующий метод.
     *
     * @param request тело запроса в виде карты, содержащей параметры callback-события.
     * @return {@link ResponseEntity} с результатом обработки запроса.
     */
    @PostMapping
    public ResponseEntity<?> handleMessage(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");

        log.info("Received callback request of type: {}", type);

        return switch (type) {
            case "confirmation" -> handleConfirmation();
            case "message_new" -> handleMessageNew(request);
            default -> {
                log.warn("Unknown event type: {}", type);
                yield ResponseEntity.badRequest().build();
            }
        };
    }

    /**
     * Возвращает код подтверждения для ВКонтакте, если запрашивается событие типа "confirmation".
     *
     * @return {@link ResponseEntity} с кодом подтверждения.
     */
    private ResponseEntity<?> handleConfirmation() {
        log.info("Returning confirmation code.");
        return ResponseEntity.ok(confirmationCode);
    }

    /**
     * Обрабатывает событие типа "message_new", извлекает текст сообщения и отправляет ответ пользователю.
     * Также проверяет, что сообщение не является устаревшим, сравнивая его временную метку с последней обработанной.
     *
     * @param request тело запроса, содержащее информацию о новом сообщении.
     * @return {@link ResponseEntity} с результатом обработки запроса.
     */
    private ResponseEntity<?> handleMessageNew(Map<String, Object> request) {
        try {
            Map<String, Object> object = (Map<String, Object>) request.get("object");
            Map<String, Object> message = (Map<String, Object>) object.get("message");

            int messageTimestamp = (Integer) message.get("date");
            if (messageTimestamp <= lastProcessedTimestamp) {
                log.info("Skipping outdated message with timestamp: {}", messageTimestamp);
                return ResponseEntity.ok().build();
            }

            lastProcessedTimestamp = messageTimestamp;

            String userId = String.valueOf(message.get("from_id"));
            String messageText = (String) message.get("text");
            String responseText = "Вы сказали: " + messageText;

            log.info("Sending response to user {}: {}", userId, responseText);
            boolean success = sendMessage(userId, responseText);

            return success ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Error while processing new message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Отправляет сообщение пользователю через API ВКонтакте.
     *
     * @param userId ID пользователя, которому отправляется сообщение.
     * @param text текст сообщения.
     * @return {@code true}, если сообщение успешно отправлено, иначе {@code false}.
     */
    public boolean sendMessage(String userId, String text) {
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

            int responseCode = connection.getResponseCode();
            log.info("VK API response code: {}", responseCode);

            return responseCode == 200;
        } catch (Exception e) {
            log.error("Error occurred while sending message: {}", e.getMessage(), e);
            return false;
        }
    }
}
