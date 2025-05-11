package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import org.springframework.context.annotation.Lazy;
import ru.derendyaev.ideathesis_bot_service.dto.topic.StudentTopicSelectionDto;
import ru.derendyaev.ideathesis_bot_service.handler.callbackData.CallbackHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

import java.util.UUID;

@Slf4j
@Component
public class CheckTopicStatusHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final UsersServiceClient usersServiceClient;

    @Autowired
    public CheckTopicStatusHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, UsersServiceClient usersServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.usersServiceClient = usersServiceClient;
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("check_status_");
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long topicId = Long.parseLong(data.split("_")[2]);
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        String studentGuid = session.getAuth().getUser().getGuid();

        topicServiceClient.getActiveTopicsForStudent(studentGuid)
                .doOnNext(activeTopics -> {
                    StudentTopicSelectionDto topic = activeTopics.stream()
                            .filter(t -> t.getTopic().getId().equals(topicId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Topic not found"));
                    usersServiceClient.getEmployeeById(topic.getSupervisorGuid().toString())
                            .doOnNext(employee -> {
                                String message = String.format(
                                        "Тема: %s\n- Статус: %s\n- Руководитель: %s\n- Описание: %s",
                                        topic.getTopic().getTitle(),
                                        topic.getTopic().getStatus().getDisplayName(),
                                        employee.getFullName(),
                                        topic.getTopic().getDescription()
                                );
                                botServiceDelegate.sendMessage(chatId, message, ParseMode.NONE);
                            })
                            .doOnError(ex -> {
                                log.error("Ошибка при получении данных преподавателя: {}", ex.getMessage());
                                botServiceDelegate.sendMessage(chatId, "Ошибка при получении данных преподавателя.", ParseMode.NONE);
                            })
                            .subscribe();
                })
                .doOnError(ex -> {
                    log.error("Ошибка при получении темы: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при получении статуса темы.", ParseMode.NONE);
                })
                .subscribe();
    }
}
