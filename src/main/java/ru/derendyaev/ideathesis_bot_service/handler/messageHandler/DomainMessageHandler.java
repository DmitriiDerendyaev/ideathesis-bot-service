package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.services.ParseMode;

@Slf4j
@Component
public class DomainMessageHandler implements MessageHandler {
    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public DomainMessageHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        botServiceDelegate.getUserStateService().getSessionData(chatId)
                .setDomains(botServiceDelegate.splitList(text));

        var session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        botServiceDelegate.sendMessage(chatId,
                String.format("Компетенции: %s\nОбласти: %s\nСпасибо! Подбираю темы...",
                        String.join(", ", session.getCompetencies()),
                        String.join(", ", session.getDomains())), ParseMode.HTML);

        botServiceDelegate.getUserStateService().clear(chatId);
    }
}