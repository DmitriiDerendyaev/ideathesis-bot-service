package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;

import java.util.List;

@Slf4j
@Component
public class CompetenciesMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public CompetenciesMessageHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        session.setCompetencies(botServiceDelegate.splitList(text));

        InlineKeyboardMarkup keyboard = null;
        if (session.getDomains() != null && !session.getDomains().isEmpty()) {
            String previousDomains = String.join(", ", session.getDomains());
            keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(
                            InlineKeyboardButton.builder()
                                    .text("Использовать предыдущие: " + previousDomains)
                                    .callbackData("use_previous_domains")
                                    .build()
                    ))
                    .build();
        }

        botServiceDelegate.sendMessageWithKeyboard(
                chatId,
                "Введите области интересов через запятую.",
                ParseMode.HTML,
                keyboard
        );
        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_DOMAIN);
    }
}