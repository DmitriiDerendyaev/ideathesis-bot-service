package ru.derendyaev.ideathesis_bot_service.handler;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.services.UserStateService;

public interface StateHandler {
    void handle(Message message, UserStateService stateService);
}
