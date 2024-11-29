//package ru.onshin.config;
//
//import com.vk.api.sdk.client.VkApiClient;
//import com.vk.api.sdk.client.actors.GroupActor;
//import com.vk.api.sdk.httpclient.HttpTransportClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Конфигурационный класс для настройки компонентов бота ВКонтакте.
// * <p>
// * Создает необходимые бины для работы с API ВКонтакте:
// * <ul>
// *     <li>{@link VkApiClient} — клиент для взаимодействия с API.</li>
// *     <li>{@link GroupActor} — объект, представляющий данные группы, от имени которой работает бот.</li>
// * </ul>
// * </p>
// */
//@Configuration
//public class VkBotConfig {
//
//    /**
//     * Создает бин {@link VkApiClient} для взаимодействия с API ВКонтакте.
//     *
//     * @return объект {@link VkApiClient}.
//     */
//    @Bean
//    public VkApiClient vkApiClient() {
//        return new VkApiClient(new HttpTransportClient());
//    }
//
//    /**
//     * Создает бин {@link GroupActor}, представляющий данные группы, от имени которой бот выполняет действия.
//     *
//     * @param groupId     ID группы ВКонтакте.
//     * @param accessToken Токен доступа для авторизации.
//     * @return объект {@link GroupActor}.
//     */
//    @Bean
//    public GroupActor groupActor(@Value("${group-id}") int groupId,
//                                 @Value("${access-token}") String accessToken) {
//        return new GroupActor(
//                groupId,
//                accessToken
//        );
//    }
//}
