package ru.derendyaev.ideathesis_bot_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.models.AuthResponse;

@Component
public class AuthBot extends TelegramLongPollingBot {
    private final UserStateService stateService;
    private final AuthServiceClient authClient;
    private final String botUsername;

    public AuthBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UserStateService stateService,
            AuthServiceClient authClient) {
        super(botToken);
        this.botUsername = botUsername;
        this.stateService = stateService;
        this.authClient = authClient;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        if ("/start".equals(text)) {
            sendResponse(chatId, "Привет! Введите логин и пароль через пробел");
            stateService.setAwaitingCredentials(chatId, true);
        } else if (stateService.isAwaitingCredentials(chatId)) {
            processCredentials(chatId, text);
        }
    }

    private void processCredentials(Long chatId, String credentials) {
        String[] parts = credentials.split(" ");
        if (parts.length != 2) {
            sendResponse(chatId, "Неверный формат ввода");
            return;
        }

        authClient.authenticate(parts[0], parts[1])
                .subscribe(
                        response -> handleAuthSuccess(chatId, response),
                        error -> handleAuthError(chatId, error)
                );
    }

    private void handleAuthSuccess(Long chatId, AuthResponse response) {
        boolean isFirstAuth = stateService.isFirstAuth(chatId);
        stateService.setFirstAuth(chatId, false);
        stateService.saveAuthData(chatId, response);
        stateService.setAwaitingCredentials(chatId, false);

        String message = isFirstAuth ?
                String.format("Добро пожаловать! %s\nГруппа: %s",
                        response.getUserData().getName(),
                        response.getUserData().getGroup()) :
                String.format("С возвращением, %s!", response.getUserData().getName());

        sendResponse(chatId, message);
        sendResponse(chatId, "Запрос компетенций...");
    }

    private void handleAuthError(Long chatId, Throwable error) {
        String message = error instanceof ServiceUnavailableException ?
                "Сервис авторизации недоступен" :
                error instanceof UnauthorizedException ?
                        "Неверные учетные данные" :
                        "Ошибка авторизации";

        sendResponse(chatId, message);
        stateService.setAwaitingCredentials(chatId, false);
    }

    private void sendResponse(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
