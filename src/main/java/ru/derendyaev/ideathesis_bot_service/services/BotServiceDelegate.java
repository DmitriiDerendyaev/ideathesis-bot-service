package ru.derendyaev.ideathesis_bot_service.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;

import java.util.List;

public interface BotServiceDelegate {
    void sendMessage(long chatId, String text, ParseMode parseMode);
    void sendMessageWithKeyboard(long chatId, String text, ParseMode parseMode, InlineKeyboardMarkup keyboard);
    UserStateService getUserStateService();
    String getBotUsername();
    String getBotToken();
    java.util.List<String> splitList(String text);
}