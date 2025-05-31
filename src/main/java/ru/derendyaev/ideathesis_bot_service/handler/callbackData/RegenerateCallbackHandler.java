package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Slf4j
@Component
@CallbackData("regenerate")
public class RegenerateCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public RegenerateCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        String previousCompetencies = String.join(", ", session.getCompetencies() != null ? session.getCompetencies() : List.of());
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("Использовать предыдущие: " + previousCompetencies)
                                .callbackData("use_previous_competencies")
                                .build()
                ))
                .build();
        botServiceDelegate.sendMessageWithKeyboard(
                chatId, "Введите компетенции через запятую.", ParseMode.NONE, keyboard
        );
        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "regenerate".equals(callbackData);
    }
}