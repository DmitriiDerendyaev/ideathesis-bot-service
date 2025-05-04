package ru.derendyaev.ideathesis_bot_service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class StartMessageHandler implements MessageHandler {
    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public StartMessageHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        if ("/start".equals(text)) {
            botServiceDelegate.sendMessage(chatId, "Привет! Введите логин и пароль через пробел.");
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_CREDENTIALS);
        }
    }
}