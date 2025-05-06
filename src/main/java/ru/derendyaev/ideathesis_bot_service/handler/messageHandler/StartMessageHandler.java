package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.services.ParseMode;
import ru.derendyaev.ideathesis_bot_service.mustache.MustacheTemplateService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StartMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final MustacheTemplateService templateService;

    @Autowired
    public StartMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, MustacheTemplateService templateService) {
        this.botServiceDelegate = botServiceDelegate;
        this.templateService = templateService;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        if ("/start".equals(text)) {
            Map<String, Object> context = new HashMap<>();
            String introMessage = templateService.render("intro_message.mustache", context);
            botServiceDelegate.sendMessage(chatId, introMessage, ParseMode.MARKDOWN);
            String requestCredentialsMessage = templateService.render("request_credentials.mustache", context);
            botServiceDelegate.sendMessage(chatId, requestCredentialsMessage, ParseMode.MARKDOWN);
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_CREDENTIALS);
        }
    }
}