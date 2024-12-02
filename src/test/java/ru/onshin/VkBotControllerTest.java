package ru.onshin;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.onshin.controller.VkBotController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;

/**
 * Unit тесты для контроллера {@link VkBotController}.
 * Проверяет работу метода {@link VkBotController#handleMessage(Map)} для обработки новых сообщений.
 * Тест проверяет успешную обработку сообщения с типом "message_new".
 */
@ExtendWith(MockitoExtension.class)
public class VkBotControllerTest {

    @InjectMocks
    private VkBotController controller;

    @Mock
    private VkBotController vkBotController;

    private Map<String, Object> messageNewRequest;

    /**
     * Инициализация данных перед каждым тестом.
     * Создает мапу с данными для теста (тип события и сообщение).
     */
    @BeforeEach
    public void setUp() {
        messageNewRequest = Map.of(
                "type", "message_new",
                "object", Map.of(
                        "message", Map.of(
                                "date", 1234567890,
                                "from_id", 123,
                                "text", "Привет!"
                        )
                )
        );
    }

    /**
     * Тестирует обработку нового сообщения в методе {@link VkBotController#handleMessage(Map)}.
     * Проверяет, что метод возвращает успешный ответ (HttpStatus.OK) при корректном запросе.
     */
    @Test
    public void testHandleMessageNew() {
        lenient().when(vkBotController.sendMessage(eq("123"), eq("Вы сказали: Привет!"))).thenReturn(true);

        ResponseEntity<?> response = controller.handleMessage(messageNewRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
