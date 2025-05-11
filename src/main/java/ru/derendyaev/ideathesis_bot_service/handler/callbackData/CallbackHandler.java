package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackHandler {
    void handleCallback(CallbackQuery callbackQuery);
    boolean canHandle(String callbackData);
}
