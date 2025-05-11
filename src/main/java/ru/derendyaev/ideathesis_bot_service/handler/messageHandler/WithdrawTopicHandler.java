package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.handler.callbackData.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;


@Slf4j
@Component
public class WithdrawTopicHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;

    @Autowired
    public WithdrawTopicHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("withdraw_");
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long topicId = Long.parseLong(data.split("_")[1]);
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        String studentGuid = session.getAuth().getUser().getGuid();

        topicServiceClient.withdrawTopic(topicId, studentGuid)
                .doOnSuccess(response -> {
                    botServiceDelegate.sendMessage(chatId, "Заявка на тему отозвана.\nНачать сначала? - /start", ParseMode.NONE);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при отзыве заявки: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Нельзя отозвать заявку на эту тему или произошла ошибка.", ParseMode.NONE);
                })
                .subscribe();
    }
}