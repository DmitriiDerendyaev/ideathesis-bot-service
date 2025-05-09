package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
public class LogoutMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public LogoutMessageHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        if ("/logout".equals(text)) {
            botServiceDelegate.getUserStateService().clear(chatId);
            botServiceDelegate.sendMessage(chatId, "Сессия завершена. Используйте /start, чтобы начать заново.", ParseMode.NONE);
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_CREDENTIALS);
        }
    }
}
