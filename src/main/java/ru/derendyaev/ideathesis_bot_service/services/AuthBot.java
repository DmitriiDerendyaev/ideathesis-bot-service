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
import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.derendyaev.ideathesis_bot_service.handler.StateHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthBot extends TelegramLongPollingBot {
    private final UserStateService stateService;
    private final Map<BotState, StateHandler> stateHandlers;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        BotState currentState = stateService.getCurrentState(chatId);

        stateHandlers.getOrDefault(currentState, (m, s) ->
                        sendResponse(chatId, "Неизвестная команда. Введите /start"))
                .handle(message, stateService);
    }

    public void sendWelcomeMessage(Long chatId) {
        String text = "Привет! 👋 Я бот, который поможет вам выбрать тему для дипломной работы!\n\n" +
                "Пожалуйста, введите ваш логин и пароль через пробел";
        sendResponse(chatId, text);
    }

    public void sendCompetenciesRequest(Long chatId) {
        String text = "Введите через запятую ваши компетенции.\nПример: UI, UX, Верстка, HTML, CSS";
        sendResponse(chatId, text);
    }

    public void sendDomainRequest(Long chatId) {
        String text = "Введите через запятую области интересов.\nПример: Блокчейн, Банковское дело";
        sendResponse(chatId, text);
    }

    public void sendTopicConfirmationRequest(Long chatId) {
        UserSessionData sessionData = stateService.getSessionData(chatId);
        String text = "Спасибо! На основе ваших данных:\n" +
                "Компетенции: " + String.join(", ", sessionData.getCompetencies()) + "\n" +
                "Области: " + String.join(", ", sessionData.getDomains()) + "\n\n" +
                "Я подобрал для вас следующие темы...";
        sendResponse(chatId, text);
    }

    public void sendResponse(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}