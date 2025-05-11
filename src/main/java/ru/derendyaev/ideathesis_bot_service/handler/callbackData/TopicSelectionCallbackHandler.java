package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GeneratedTopicDto;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

@Slf4j
@Component
@CallbackData("select_")
public class TopicSelectionCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public TopicSelectionCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        if (data.startsWith("select_") && !data.startsWith("select_supervisor_")) {
            Long topicId = Long.parseLong(data.split("_")[1]);
            String studentGuid = session.getAuth().getUser().getGuid();

            topicServiceClient.getLastTenTopics(studentGuid)
                    .doOnNext(lastTenTopics -> {
                        GeneratedTopicDto selectedTopic = session.getGeneratedTopics().getTopics().stream()
                                .filter(t -> t.getId().equals(topicId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
                        session.setSelectedTopic(selectedTopic);

                        String confirmationMessage = messageUtils.buildTopicConfirmationMessage(selectedTopic, lastTenTopics);
                        botServiceDelegate.sendMessageWithKeyboard(
                                chatId, confirmationMessage, ParseMode.MARKDOWN, messageUtils.createConfirmationKeyboard(selectedTopic, lastTenTopics)
                        );
                        botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_CONFIRMATION);
                    })
                    .doOnError(ex -> {
                        log.error("Ошибка при получении истории тем: {}", ex.getMessage());
                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
                    })
                    .subscribe();
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("select_") && !callbackData.startsWith("select_supervisor_");
    }
}
