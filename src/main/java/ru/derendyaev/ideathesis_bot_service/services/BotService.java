package ru.derendyaev.ideathesis_bot_service.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import ru.derendyaev.ideathesis_bot_service.models.BotState;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
    private final UserStateService stateService;
    private final AuthServiceClient authClient;

    @Value("${telegram.bot.token}")
    private String token;
    @Value("${telegram.bot.username}")
    private String username;

    @PostConstruct
    public void register() throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message msg) {
        long chatId = msg.getChatId();
        BotState state = stateService.getState(chatId);
        String text = msg.getText().trim();

        switch (state) {
            case START:
                if ("/start".equals(text)) {
                    sendMessage(chatId, "Привет! Введите логин и пароль через пробел.");
                    stateService.setState(chatId, BotState.AWAITING_CREDENTIALS);
                }
                break;
            case AWAITING_CREDENTIALS:
                String[] parts = text.split(" ");
                if (parts.length != 2) {
                    sendMessage(chatId, "Неверный формат. Введите: логин пароль");
                } else {
                    authClient.authenticate(parts[0], parts[1])
                            .doOnNext(resp -> {
                                // Успешная аутентификация
                                log.info("Authenticated: {}", resp.getUser());
                                stateService.setState(chatId, BotState.AWAITING_COMPETENCIES);
                                stateService.saveAuth(chatId, resp);
                                sendMessage(chatId, "Авторизация успешна. Введите ваши компетенции:");
                            })
                            .doOnError(UnauthorizedException.class, ex -> {
                                log.warn("Неверные учетные данные: {}", parts[0]);
                                sendMessage(chatId, "Неверный логин или пароль. Попробуйте снова:");
                            })
                            .doOnError(ServiceUnavailableException.class, ex -> {
                                log.error("Сервис авторизации недоступен");
                                sendMessage(chatId, "Сервис временно недоступен. Попробуйте позже.");
                            })
                            .subscribe(); // обязательно подписка в реактивном стиле
                }
                break;
            case AWAITING_COMPETENCIES:
                stateService.getSessionData(chatId).setCompetencies(splitList(text));
                sendMessage(chatId, "Введите области интересов через запятую.");
                stateService.setState(chatId, BotState.AWAITING_DOMAIN);
                break;
            case AWAITING_DOMAIN:
                stateService.getSessionData(chatId).setDomains(splitList(text));
                var session = stateService.getSessionData(chatId);
                sendMessage(chatId,
                        String.format("Компетенции: %s\nОбласти: %s\nСпасибо! Подбираю темы...",
                                String.join(", ", session.getCompetencies()),
                                String.join(", ", session.getDomains())));
                stateService.clear(chatId);
                break;
        }
    }

    private java.util.List<String> splitList(String text) {
        return java.util.Arrays.stream(text.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .toList();
    }

    private void sendMessage(long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(Long.toString(chatId)).text(text).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() { return username; }

    @Override
    public String getBotToken() { return token; }
}