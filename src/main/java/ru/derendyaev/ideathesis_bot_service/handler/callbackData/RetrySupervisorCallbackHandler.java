package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
@Slf4j

@Component
@CallbackData("retry_supervisor")
public class RetrySupervisorCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public RetrySupervisorCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        botServiceDelegate.sendMessage(chatId, "Введите ФИО преподавателя для выбора руководителя.", ParseMode.NONE);
        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_SUPERVISOR);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "retry_supervisor".equals(callbackData);
    }
}
