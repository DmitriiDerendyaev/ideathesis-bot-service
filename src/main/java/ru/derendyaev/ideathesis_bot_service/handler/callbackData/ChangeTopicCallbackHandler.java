package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GeneratedTopicDto;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

@Slf4j
@Component
@CallbackData("change_select_")
public class ChangeTopicCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public ChangeTopicCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        Long topicId = Long.parseLong(data.split("_")[2]);
        String studentGuid = session.getAuth().getUser().getGuid();

        topicServiceClient.getLastTenTopics(studentGuid)
                .doOnNext(lastTenTopics -> {
                    GeneratedTopicDto newTopic = lastTenTopics.stream()
                            .filter(t -> t.getId().equals(topicId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
                    session.setSelectedTopic(newTopic);

                    String confirmationMessage = messageUtils.buildTopicConfirmationMessage(newTopic, lastTenTopics);
                    botServiceDelegate.sendMessageWithKeyboard(
                            chatId, confirmationMessage, ParseMode.MARKDOWN, messageUtils.createConfirmationKeyboard(newTopic, lastTenTopics)
                    );
                })
                .doOnError(ex -> {
                    log.error("Ошибка при получении истории тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
                })
                .subscribe();
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("change_select_");
    }
}
