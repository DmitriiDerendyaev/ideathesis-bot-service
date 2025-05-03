package ru.derendyaev.ideathesis_bot_service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.services.AuthBot;
import ru.derendyaev.ideathesis_bot_service.services.UserStateService;

@Service
@RequiredArgsConstructor
public class StartStateHandler implements StateHandler {
    private final AuthBot bot;

    @Override
    public void handle(Message message, UserStateService stateService) {
        Long chatId = message.getChatId();
        if ("/start".equals(message.getText())) {
            bot.sendWelcomeMessage(chatId);
            stateService.setState(chatId, BotState.AWAITING_CREDENTIALS);
        }
    }
}