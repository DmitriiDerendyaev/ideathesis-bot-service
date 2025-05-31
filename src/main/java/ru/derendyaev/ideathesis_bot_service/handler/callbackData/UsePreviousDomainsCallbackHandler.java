package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

import java.util.List;

@Slf4j
@Component
@CallbackData("use_previous_domains")
public class UsePreviousDomainsCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public UsePreviousDomainsCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        String studentGuid = session.getAuth().getUser().getGuid();
        botServiceDelegate.sendMessage(chatId, "Подбираю темы... ⏳", ParseMode.NONE);

        topicServiceClient.generateTopics(studentGuid, new GenerateTopicRequest(
                        String.join(",", session.getCompetencies() != null ? session.getCompetencies() : List.of()),
                        String.join(",", session.getDomains() != null ? session.getDomains() : List.of()),
                        "BACHELOR"
                ))
                .doOnNext(response -> {
                    session.setGeneratedTopics(response);
                    String topicsMessage = messageUtils.buildTopicsMessage(response);
                    botServiceDelegate.sendMessageWithKeyboard(
                            chatId,
                            topicsMessage,
                            ParseMode.MARKDOWN,
                            messageUtils.createTopicSelectionKeyboard(response)
                    );
                })
                .doOnError(ex -> {
                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
                })
                .doOnSuccess(response -> botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_SELECTION))
                .subscribe();
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "use_previous_domains".equals(callbackData);
    }
}
