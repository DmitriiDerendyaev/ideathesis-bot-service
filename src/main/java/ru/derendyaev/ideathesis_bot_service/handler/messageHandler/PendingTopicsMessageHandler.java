package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
public class PendingTopicsMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public PendingTopicsMessageHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        botServiceDelegate.sendMessage(chatId, "Пожалуйста, используйте кнопки для управления темами.", ParseMode.NONE);
    }
}
