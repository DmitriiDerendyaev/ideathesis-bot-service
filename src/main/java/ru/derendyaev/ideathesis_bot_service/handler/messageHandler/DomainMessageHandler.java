package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

@Slf4j
@Component
public class DomainMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public DomainMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        session.setDomains(botServiceDelegate.splitList(text));

        String studentGuid = session.getAuth().getUser().getGuid();
        botServiceDelegate.sendMessage(chatId, messageUtils.buildSummaryMessage(session), ParseMode.HTML);

        // Отправляем сообщение с индикатором загрузки
        botServiceDelegate.sendMessage(chatId, "Подбираю темы... ⏳", ParseMode.NONE);

        topicServiceClient.generateTopics(studentGuid, messageUtils.createGenerateTopicRequest(session))
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
}