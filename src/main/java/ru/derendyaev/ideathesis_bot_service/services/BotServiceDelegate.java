package ru.derendyaev.ideathesis_bot_service.services;

import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;

import java.util.List;

public interface BotServiceDelegate {
    void sendMessage(long chatId, String text, ParseMode parseMode);
    UserStateService getUserStateService();

    List<String> splitList(String text);
}