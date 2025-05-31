package ru.derendyaev.ideathesis_bot_service.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.derendyaev.ideathesis_bot_service.handler.UpdateDispatcher;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot implements BotServiceDelegate {
    private final UserStateService stateService;
    private final UpdateDispatcher updateDispatcher;

    @Value("${app.values.bot.token}")
    @Setter
    private String token;

    @Value("${app.values.bot.username}")
    @Setter
    private String username;

    @PostConstruct
    public void register() throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateDispatcher.dispatch(update);
    }

    @Override
    public void sendMessage(long chatId, String text, ParseMode parseMode) {
        try {
            execute(SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode(parseMode != ParseMode.NONE ? parseMode.getValue() : null)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void sendMessageWithKeyboard(long chatId, String text, ParseMode parseMode, InlineKeyboardMarkup keyboard) {
        try {
            execute(SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode(parseMode != ParseMode.NONE ? parseMode.getValue() : null)
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message with keyboard", e);
        }
    }

    @Override
    public UserStateService getUserStateService() {
        return stateService;
    }

    @Override
    public List<String> splitList(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}