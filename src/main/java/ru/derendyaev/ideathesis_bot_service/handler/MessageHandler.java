package ru.derendyaev.ideathesis_bot_service.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {
    void handle(Message msg);
}
