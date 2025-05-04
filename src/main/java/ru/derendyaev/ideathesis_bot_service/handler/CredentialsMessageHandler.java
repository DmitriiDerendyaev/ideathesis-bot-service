package ru.derendyaev.ideathesis_bot_service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class CredentialsMessageHandler implements MessageHandler {
    private final BotServiceDelegate botServiceDelegate;
    private final AuthServiceClient authServiceClient;

    @Autowired
    public CredentialsMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, AuthServiceClient authServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.authServiceClient = authServiceClient;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            botServiceDelegate.sendMessage(chatId, "Неверный формат. Введите: логин пароль");
        } else {
            authServiceClient.authenticate(parts[0], parts[1])
                    .doOnNext(resp -> {
                        log.info("Authenticated: {}", resp.getUser());
                        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
                        botServiceDelegate.getUserStateService().saveAuth(chatId, resp);
                        botServiceDelegate.sendMessage(chatId, "Авторизация успешна. Введите ваши компетенции:");
                    })
                    .doOnError(UnauthorizedException.class, ex -> {
                        log.warn("Неверные учетные данные: {}", parts[0]);
                        botServiceDelegate.sendMessage(chatId, "Неверный логин или пароль. Попробуйте снова:");
                    })
                    .doOnError(ServiceUnavailableException.class, ex -> {
                        log.error("Сервис авторизации недоступен");
                        botServiceDelegate.sendMessage(chatId, "Сервис временно недоступен. Попробуйте позже.");
                    })
                    .subscribe();
        }
    }
}