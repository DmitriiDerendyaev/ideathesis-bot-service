package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.handler.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
public class TopicSelectionHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;

    public TopicSelectionHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        if ("regenerate".equals(data)) {
            // Повторная генерация тем
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
            botServiceDelegate.sendMessage(chatId, "Введите компетенции через запятую.", ParseMode.NONE);
        } else if (data.startsWith("select_")) {
            // Выбор темы
            Long topicId = Long.parseLong(data.split("_")[1]);
            String studentGuid = session.getAuth().getUser().getGuid();

            topicServiceClient.selectTopic(studentGuid, topicId)
                    .doOnSuccess(response -> {
                        botServiceDelegate.sendMessage(chatId, "Тема успешно выбрана! Спасибо.", ParseMode.NONE);
                        botServiceDelegate.getUserStateService().clear(chatId);
                    })
                    .doOnError(ex -> {
                        log.error("Ошибка при выборе темы: {}", ex.getMessage());
                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка при выборе темы. Попробуйте снова.", ParseMode.NONE);
                    })
                    .subscribe();
        }
    }
}
