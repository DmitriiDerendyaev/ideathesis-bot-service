package ru.derendyaev.ideathesis_bot_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.derendyaev.ideathesis_bot_service.services.BotService;

//@Configuration
//public class BotInitializer {
//
//    /**
//     * Регистрирует бота в Telegram API
//     *
//     * @param authBot экземпляр вашего бота
//     * @return TelegramBotsApi
//     * @throws TelegramApiException если регистрация не удалась
//     */
//    @Bean
//    public TelegramBotsApi telegramBotsApi(BotService authBot) throws TelegramApiException {
//        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
//        try {
//            telegramBotsApi.registerBot(authBot);
//            System.out.println("Бот успешно зарегистрирован: @" + authBot.getBotUsername());
//        } catch (TelegramApiException e) {
//            System.err.println("Ошибка регистрации бота: " + e.getMessage());
//            throw e; // Пробрасываем исключение дальше для остановки контекста Spring
//        }
//        return telegramBotsApi;
//    }
//}