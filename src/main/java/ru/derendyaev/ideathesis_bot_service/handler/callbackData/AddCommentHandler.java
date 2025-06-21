package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Component
public class AddCommentHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final UserStateService stateService;

    @Autowired
    public AddCommentHandler(@Lazy BotServiceDelegate botServiceDelegate, UserStateService stateService) {
        this.botServiceDelegate = botServiceDelegate;
        this.stateService = stateService;
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("add_comment_");
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long topicId = Long.parseLong(data.split("_")[2]);
        long chatId = callbackQuery.getMessage().getChatId();

        stateService.setState(chatId, BotState.AWAITING_COMMENT);
        UserSessionData sessionData = stateService.getSessionData(chatId);
        //sessionData.setCurrentTopicId(topicId);

        botServiceDelegate.sendMessage(chatId, "Пожалуйста, отправьте ваш комментарий.", ParseMode.NONE);
    }
}