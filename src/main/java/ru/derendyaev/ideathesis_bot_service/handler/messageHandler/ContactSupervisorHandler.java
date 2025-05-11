package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.StudentTopicSelectionDto;
import ru.derendyaev.ideathesis_bot_service.handler.callbackData.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
public class ContactSupervisorHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final UsersServiceClient usersServiceClient;

    @Autowired
    public ContactSupervisorHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, UsersServiceClient usersServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.usersServiceClient = usersServiceClient;
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("contact_supervisor_");
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long topicId = Long.parseLong(data.split("_")[2]);
        long chatId = callbackQuery.getMessage().getChatId();
        String studentGuid = botServiceDelegate.getUserStateService().getSessionData(chatId).getAuth().getUser().getGuid();

        log.info("Handling contact_supervisor for topic ID: {} and student GUID: {}", topicId, studentGuid);

        topicServiceClient.getActiveTopicsForStudent(studentGuid)
                .doOnNext(activeTopics -> {
                    StudentTopicSelectionDto topic = activeTopics.stream()
                            .filter(t -> t.getTopic().getId().equals(topicId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Topic not found"));
                    log.info("Found topic for contact: {} (supervisor GUID: {})",
                            topic.getTopic().getTitle(), topic.getSupervisorGuid());

                    usersServiceClient.getEmployeeById(topic.getSupervisorGuid().toString())
                            .doOnNext(employee -> {
                                String supervisorName = employee.getFullName();
                                log.info("Successfully retrieved supervisor for contact: {}", supervisorName);
                                String message = String.format(
                                        "Данная функциональность сейчас на доработке, вы можете братиться к преподавателю %s в ВУЗе.",
                                        supervisorName
                                );
                                botServiceDelegate.sendMessage(chatId, message, ParseMode.NONE);
                            })
                            .doOnError(ex -> {
                                log.error("Ошибка при получении данных преподавателя для GUID {}: {}: {}",
                                        topic.getSupervisorGuid(), ex.getClass().getName(), ex.getMessage(), ex);
                                botServiceDelegate.sendMessage(chatId, "Ошибка при получении данных преподавателя.", ParseMode.NONE);
                            })
                            .subscribe();
                })
                .doOnError(ex -> {
                    log.error("Ошибка при получении темы с ID {}: {}: {}",
                            topicId, ex.getClass().getName(), ex.getMessage(), ex);
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при получении данных темы.", ParseMode.NONE);
                })
                .subscribe();
    }
}