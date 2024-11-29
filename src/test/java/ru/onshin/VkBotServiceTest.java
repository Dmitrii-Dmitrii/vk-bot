//package ru.onshin;
//
//import com.vk.api.sdk.actions.Messages;
//import com.vk.api.sdk.client.VkApiClient;
//import com.vk.api.sdk.client.actors.GroupActor;
//import com.vk.api.sdk.objects.messages.Message;
//import com.vk.api.sdk.queries.messages.MessagesSendQuery;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import ru.onshin.service.VkBotService;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.anyInt;
//import static org.mockito.Mockito.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
///**
// * Тестовый класс для проверки работы сервиса {@link VkBotService}.
// * Этот класс содержит тесты, которые проверяют логику обработки сообщений
// * и отправку ответа через VK API.
// */
//class VkBotServiceTest {
//    private VkApiClient vkApiClient;
//    private GroupActor groupActor;
//    private VkBotService vkBotService;
//
//    /**
//     * Инициализация моков и объектов перед каждым тестом.
//     * Этот метод выполняется перед каждым тестом, чтобы настроить
//     * моки и создать экземпляр {@link VkBotService}.
//     */
//    @BeforeEach
//    void setUp() {
//        vkApiClient = mock(VkApiClient.class);
//        groupActor = mock(GroupActor.class);
//        Messages messagesMock = mock(Messages.class);
//        when(vkApiClient.messages()).thenReturn(messagesMock);
//        vkBotService = new VkBotService(vkApiClient, groupActor);
//    }
//
//    /**
//     * Тестирует метод {@link VkBotService#processMessage(Message)}.
//     * Этот тест проверяет, что метод {@link VkBotService#processMessage(Message)}
//     * корректно обрабатывает входящее сообщение и отправляет ответ пользователю.
//     * Ожидается, что ответное сообщение будет в формате "Вы сказали: {исходное сообщение}".
//     */
//    @Test
//    void processMessage_shouldSendReply() {
//        Message message = new Message();
//        message.setFromId(123);
//        message.setText("Привет!");
//
//        MessagesSendQuery sendQuery = mock(MessagesSendQuery.class);
//        when(vkApiClient.messages().send(groupActor)).thenReturn(sendQuery);
//        when(sendQuery.userId(anyInt())).thenReturn(sendQuery);
//        when(sendQuery.randomId(anyInt())).thenReturn(sendQuery);
//        when(sendQuery.message(anyString())).thenReturn(sendQuery);
//
//        vkBotService.processMessage(message);
//
//        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
//        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
//
//        verify(sendQuery).userId(userIdCaptor.capture());
//        verify(sendQuery).message(messageCaptor.capture());
//
//        assertEquals(123, userIdCaptor.getValue());
//        assertEquals("Вы сказали: Привет!", messageCaptor.getValue());
//    }
//}