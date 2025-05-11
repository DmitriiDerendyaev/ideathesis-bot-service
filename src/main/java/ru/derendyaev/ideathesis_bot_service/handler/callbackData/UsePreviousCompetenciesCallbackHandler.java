package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

import java.util.List;

@Slf4j
@Component
@CallbackData("use_previous_competencies")
public class UsePreviousCompetenciesCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public UsePreviousCompetenciesCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        String previousDomains = String.join(", ", session.getDomains() != null ? session.getDomains() : List.of());
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("Использовать предыдущие: " + previousDomains)
                                .callbackData("use_previous_domains")
                                .build()
                ))
                .build();
        botServiceDelegate.sendMessageWithKeyboard(
                chatId, "Введите области интересов через запятую.", ParseMode.NONE, keyboard
        );
        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_DOMAIN);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "use_previous_competencies".equals(callbackData);
    }
}
