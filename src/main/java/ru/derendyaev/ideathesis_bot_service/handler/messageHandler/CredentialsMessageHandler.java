package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.mustache.UserContext;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.services.ParseMode;
import ru.derendyaev.ideathesis_bot_service.mustache.MustacheTemplateService;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class CredentialsMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final AuthServiceClient authServiceClient;
    private final MustacheTemplateService templateService;
    private final UsersServiceClient usersServiceClient; // Добавлено

    @Autowired
    public CredentialsMessageHandler(
            @Lazy BotServiceDelegate botServiceDelegate,
            AuthServiceClient authServiceClient,
            UsersServiceClient usersServiceClient, // Добавлено
            MustacheTemplateService templateService
    ) {
        this.botServiceDelegate = botServiceDelegate;
        this.authServiceClient = authServiceClient;
        this.usersServiceClient = usersServiceClient;
        this.templateService = templateService;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            botServiceDelegate.sendMessage(chatId, "Неверный формат. Введите: логин пароль", ParseMode.NONE);
        } else {
            authServiceClient.authenticate(parts[0], parts[1])
                    .doOnNext(resp -> {
                        log.info("Authenticated: {}", resp.getUser());
                        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
                        botServiceDelegate.getUserStateService().saveAuth(chatId, resp);

                        UserContext context = new UserContext(
                                resp.getUser().getFirstName(),
                                resp.getUser().getLastName(),
                                resp.getUser().getEmail(),
                                resp.getUser().getPhone(),
                                resp.getUser().getUserType()
                        );
                        String userInfoMessage = templateService.render("user_info.mustache", context);
                        botServiceDelegate.sendMessage(chatId, userInfoMessage, ParseMode.MARKDOWN);
                        String competenciesMessage = templateService.render("request_competencies.mustache", context);
                        botServiceDelegate.sendMessage(chatId, competenciesMessage, ParseMode.MARKDOWN);
                    })
                    .doOnError(UnauthorizedException.class, ex -> {
                        log.warn("Неверные учетные данные: {}", parts[0]);
                        botServiceDelegate.sendMessage(chatId, "Неверный логин или пароль. Попробуйте снова:", ParseMode.NONE);
                    })
                    .doOnError(ServiceUnavailableException.class, ex -> {
                        log.error("Сервис авторизации недоступен");
                        botServiceDelegate.sendMessage(chatId, "Сервис временно недоступен. Попробуйте позже.", ParseMode.NONE);
                    })
                    .subscribe();
        }
    }
}