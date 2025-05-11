package ru.derendyaev.ideathesis_bot_service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.derendyaev.ideathesis_bot_service.handler.callbackData.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {
    private final UserStateService stateService;
    private final Map<BotState, MessageHandler> messageHandlers;
    private final List<CallbackHandler> callbackHandlers;
    private final MessageHandler startMessageHandler;
    private final MessageHandler logoutMessageHandler;

    public void dispatch(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText().trim();

            if ("/start".equals(text)) {
                startMessageHandler.handle(msg);
            } else if ("/logout".equals(text)) {
                logoutMessageHandler.handle(msg);
            } else {
                handleMessage(msg);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message msg) {
        long chatId = msg.getChatId();
        BotState state = stateService.getState(chatId);

        MessageHandler handler = messageHandlers.get(state);
        if (handler != null) {
            handler.handle(msg);
        } else {
            log.warn("No handler found for state: {}", state);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        callbackHandlers.stream()
                .filter(handler -> handler.canHandle(data))
                .findFirst()
                .ifPresent(handler -> handler.handleCallback(callbackQuery));
    }
}