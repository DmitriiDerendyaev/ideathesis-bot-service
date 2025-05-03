package ru.derendyaev.ideathesis_bot_service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.services.AuthBot;
import ru.derendyaev.ideathesis_bot_service.services.UserStateService;

@Service
@RequiredArgsConstructor
public class CredentialsStateHandler implements StateHandler {
    private final AuthBot bot;
    private final AuthServiceClient authClient;

    @Override
    public void handle(Message message, UserStateService stateService) {
        Long chatId = message.getChatId();
        String[] parts = message.getText().split(" ");

        if (parts.length != 2) {
            bot.sendResponse(chatId, "Неверный формат ввода. Введите логин и пароль через пробел");
            return;
        }

        authClient.authenticate(parts[0], parts[1])
                .doOnError(error -> bot.sendResponse(chatId, "Ошибка авторизации: " + error.getMessage()))
                .subscribe(
                        response -> handleAuthSuccess(chatId, response, stateService),
                        error -> handleAuthError(chatId, error, stateService)
                );
    }

    private void handleAuthSuccess(Long chatId, AuthResponse response, UserStateService stateService) {
        stateService.saveAuthData(chatId, response);
        stateService.setState(chatId, BotState.AWAITING_COMPETENCIES);
        bot.sendCompetenciesRequest(chatId);
    }

    private void handleAuthError(Long chatId, Throwable error, UserStateService stateService) {
        stateService.setState(chatId, BotState.START);
    }
}