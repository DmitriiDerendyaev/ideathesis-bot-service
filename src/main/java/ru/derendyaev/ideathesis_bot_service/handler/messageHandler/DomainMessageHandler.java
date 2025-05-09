package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
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

        // Обновляем сессию с доменами
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        session.setDomains(botServiceDelegate.splitList(text));

        // Извлекаем studentGuid и создаём запрос
        String studentGuid = session.getAuth().getUser().getGuid();
        botServiceDelegate.sendMessage(chatId, messageUtils.buildSummaryMessage(session), ParseMode.HTML);

        // Отправляем запрос в topic-service
        topicServiceClient.generateTopics(studentGuid, messageUtils.createGenerateTopicRequest(session))
                .doOnNext(response -> {
                    // Формируем и отправляем сообщение с темами
                    botServiceDelegate.sendMessage(chatId, messageUtils.buildTopicsMessage(response), ParseMode.MARKDOWN);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
                })
                .doOnSuccess(response -> botServiceDelegate.getUserStateService().clear(chatId))
                .subscribe();
    }
}