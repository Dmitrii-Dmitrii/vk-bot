package ru.onshin;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.onshin.controller.VkBotController;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class VkBotControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VkBotController vkBotController;

    @Value("${vk.confirmation.code}")
    private String confirmationCode;

    @Value("${vk.my.user.id}")
    private String myUserId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(vkBotController).build();
    }

    @Test
    void testHandleMessage_Confirmation() throws Exception {
        Map<String, Object> request = Map.of("type", "confirmation");

        when(vkBotController.handleMessage(request)).thenReturn(ResponseEntity.ok("confirmation_code"));

        mockMvc.perform(post("/").contentType("application/json").content("{\"type\":\"confirmation\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testHandleNewMessageFromOtherUser() throws Exception {
        String messageJson = "{\n" +
                "  \"type\": \"message_new\",\n" +
                "  \"event_id\": \"event_1\",\n" +
                "  \"object\": {\n" +
                "    \"message\": {\n" +
                "      \"from_id\": \"12345\",\n" +
                "      \"text\": \"Hello\",\n" +
                "      \"date\": 1680000000\n" +
                "    }\n" +
                "  }\n" +
                "}";

        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testHandleNewMessageFromMyUser() throws Exception {
        String messageJson = "{\n" +
                "  \"type\": \"message_new\",\n" +
                "  \"event_id\": \"event_2\",\n" +
                "  \"object\": {\n" +
                "    \"message\": {\n" +
                "      \"from_id\": \"" + myUserId + "\",\n" +
                "      \"text\": \"Hello, bot!\",\n" +
                "      \"date\": 1680000010\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // Проверяем, что бот отправляет ответ
        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // Дополнительно можно проверить, что метод `sendMessage` был вызван с правильными параметрами
        verify(vkBotController, times(1)).sendMessage(myUserId, "Вы сказали: Hello, bot!");
    }

    @Test
    public void testHandleRepeatedEvent() throws Exception {
        String messageJson = "{\n" +
                "  \"type\": \"message_new\",\n" +
                "  \"event_id\": \"event_3\",\n" +
                "  \"object\": {\n" +
                "    \"message\": {\n" +
                "      \"from_id\": \"" + myUserId + "\",\n" +
                "      \"text\": \"Repeated message\",\n" +
                "      \"date\": 1680000020\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // Имитируем первый вызов обработки сообщения
        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // Имитируем повторный вызов с тем же eventId
        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // Проверяем, что метод `sendMessage` был вызван только один раз
        verify(vkBotController, times(1)).sendMessage(myUserId, "Вы сказали: Repeated message");
    }

    @Test
    public void testHandleOldMessage() throws Exception {
        String messageJson = "{\n" +
                "  \"type\": \"message_new\",\n" +
                "  \"event_id\": \"event_4\",\n" +
                "  \"object\": {\n" +
                "    \"message\": {\n" +
                "      \"from_id\": \"" + myUserId + "\",\n" +
                "      \"text\": \"Old message\",\n" +
                "      \"date\": 1679999999\n" +
                "    }\n" +
                "  }\n" +
                "}";

        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
