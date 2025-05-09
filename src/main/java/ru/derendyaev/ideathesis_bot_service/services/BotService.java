package ru.derendyaev.ideathesis_bot_service.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.handler.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
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
    private final AuthServiceClient authClient;
    private final Map<BotState, MessageHandler> handlers;
    private final CallbackHandler callbackHandler;
    private final MessageHandler startMessageHandler;
    private final MessageHandler logoutMessageHandler;

    @Value("${app.values.bot.token}")
    private String token;

    @Value("${app.values.bot.username}")
    private String username;

    @PostConstruct
    public void register() throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText().trim();

            // Обрабатываем команды /start и /logout независимо от состояния
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

        MessageHandler handler = handlers.get(state);
        if (handler != null) {
            handler.handle(msg);
        } else {
            log.warn("No handler found for state: {}", state);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        callbackHandler.handleCallback(callbackQuery);
    }

    public void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, ParseMode.NONE);
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