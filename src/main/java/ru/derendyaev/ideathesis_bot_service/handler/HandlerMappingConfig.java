package ru.derendyaev.ideathesis_bot_service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.derendyaev.ideathesis_bot_service.handler.messageHandler.CompetenciesMessageHandler;
import ru.derendyaev.ideathesis_bot_service.handler.messageHandler.CredentialsMessageHandler;
import ru.derendyaev.ideathesis_bot_service.handler.messageHandler.DomainMessageHandler;
import ru.derendyaev.ideathesis_bot_service.handler.messageHandler.StartMessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class HandlerMappingConfig {

    @Autowired
    private List<MessageHandler> messageHandlers;

    @Bean
    public Map<BotState, MessageHandler> handlerMapping() {
        Map<BotState, MessageHandler> mapping = new HashMap<>();
        for (MessageHandler handler : messageHandlers) {
            if (handler instanceof StartMessageHandler) {
                mapping.put(BotState.START, handler);
            } else if (handler instanceof CredentialsMessageHandler) {
                mapping.put(BotState.AWAITING_CREDENTIALS, handler);
            } else if (handler instanceof CompetenciesMessageHandler) {
                mapping.put(BotState.AWAITING_COMPETENCIES, handler);
            } else if (handler instanceof DomainMessageHandler) {
                mapping.put(BotState.AWAITING_DOMAIN, handler);
            }
        }
        return mapping;
    }
}