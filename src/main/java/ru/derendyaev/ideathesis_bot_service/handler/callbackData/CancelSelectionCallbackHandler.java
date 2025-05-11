package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

@Slf4j
@Component
@CallbackData("cancel_select")
public class CancelSelectionCallbackHandler implements CallbackHandler {
    private final BotServiceDelegate botServiceDelegate;
    private final MessageUtils messageUtils;

    @Autowired
    public CancelSelectionCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        botServiceDelegate.sendMessageWithKeyboard(
                chatId,
                messageUtils.buildTopicsMessage(session.getGeneratedTopics()),
                ParseMode.MARKDOWN,
                messageUtils.createTopicSelectionKeyboard(session.getGeneratedTopics())
        );
        botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_SELECTION);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "cancel_select".equals(callbackData);
    }
}
