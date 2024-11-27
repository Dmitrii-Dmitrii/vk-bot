package ru.onshin.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления функционалом бота ВКонтакте.
 * <p>
 * Бот слушает входящие сообщения с использованием Long Poll API ВКонтакте и отвечает на каждое сообщение.
 * Работает в отдельном потоке после запуска приложения.
 * В этом классе используется логгирование с помощью SLF4J, что позволяет отслеживать процесс работы бота.
 * </p>
 *
 * <h2>Основные функции:</h2>
 * <ul>
 *     <li>Подключение к Long Poll API для получения обновлений.</li>
 *     <li>Обработка входящих сообщений и отправка ответов.</li>
 *     <li>Непрерывная работа в фоне с использованием Spring.</li>
 * </ul>
 *
 * <h3>Компоненты:</h3>
 * <ul>
 *     <li>{@link VkApiClient} — клиент для взаимодействия с API ВКонтакте.</li>
 *     <li>{@link GroupActor} — данные группы, от имени которой работает бот.</li>
 * </ul>
 * <p>
 * Логгирование в этом классе выполняется с использованием аннотации {@link lombok.extern.slf4j.Slf4j}.
 * Логгируются основные события, такие как запуск бота, подключение к Long Poll серверу, обработка сообщений
 * и отправка ответов пользователям.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VkBotService {
    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;

    /**
     * Метод, вызываемый после создания бина. Запускает работу бота в отдельном потоке.
     * <p>
     * Логгируется запуск бота с помощью {@link #log}.
     * </p>
     */
    @PostConstruct
    public void startBot() {
        log.info("Bot started...");
        new Thread(this::runBot).start();
    }

    /**
     * Основной цикл работы бота.
     * <p>
     * Подключается к Long Poll серверу ВКонтакте, получает обновления и обрабатывает входящие сообщения.
     * Логгируются события подключения и обработки сообщений.
     * </p>
     */
    @Scheduled(fixedRate = 250)
    public void runBot() {
        try {
            log.info("Connecting to VK Long Poll server...");
            Integer ts = vkApiClient.messages().getLongPollServer(groupActor).execute().getTs();

            while (true) {
                MessagesGetLongPollHistoryQuery historyQuery = vkApiClient.messages().getLongPollHistory(groupActor).ts(ts);
                List<Message> messages = historyQuery.execute().getMessages().getItems();

                if (!messages.isEmpty()) {
                    messages.forEach(this::processMessage);
                }

                ts = vkApiClient.messages().getLongPollServer(groupActor).execute().getTs();
                Thread.sleep(250);
            }
        } catch (Exception e) {
            log.error("Error occurred while processing messages", e);
        }
    }

    /**
     * Обрабатывает входящее сообщение и отправляет ответ пользователю.
     * <p>
     * Логируется текст входящего сообщения и успешное или неудачное отправление ответа пользователю.
     * </p>
     *
     * @param message объект сообщения, полученный от Long Poll API.
     */
    public void processMessage(Message message) {
        log.debug("Processing message: {}", message.getText());
        Random random = new Random();
        try {
            vkApiClient.messages()
                    .send(groupActor)
                    .userId(message.getFromId())
                    .randomId(random.nextInt(10000))
                    .message("Вы сказали: " + message.getText())
                    .execute();
            log.info("Replied to user {}", message.getFromId());
        } catch (ApiException | ClientException e) {
            log.error("Failed to send message to user {}", message.getFromId(), e);
        }
    }
}
