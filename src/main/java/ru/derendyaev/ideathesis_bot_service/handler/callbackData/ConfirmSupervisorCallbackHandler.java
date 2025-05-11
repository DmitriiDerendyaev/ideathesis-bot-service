package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
@CallbackData("confirm_supervisor")
public class ConfirmSupervisorCallbackHandler implements CallbackHandler {
    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;

    @Autowired
    public ConfirmSupervisorCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        if (session.getSupervisor() != null && session.getSelectedTopic() != null) {
            String studentGuid = session.getAuth().getUser().getGuid();
            String supervisorGuid = session.getSupervisor().getGuid().toString();

            topicServiceClient.selectTopic(studentGuid, session.getSelectedTopic().getId(), supervisorGuid)
                    .doOnSuccess(response -> {
                        botServiceDelegate.sendMessage(chatId, "Тема успешно выбрана и отправлена на согласование преподавателю.\n\nОжидайте ответное сообщение!", ParseMode.NONE);
                        botServiceDelegate.getUserStateService().setState(chatId, BotState.COMPLETED);

                        botServiceDelegate.sendMessage(chatId, "Вы можете начать процесс заново, нажав /start\n\nИли завершить сессию вызвав команду /logout", ParseMode.NONE);
                    })
                    .doOnError(ex -> {
                        log.error("Ошибка при выборе темы: {}", ex.getMessage());
                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка при выборе темы. Попробуйте снова.", ParseMode.NONE);
                    })
                    .subscribe();
        } else {
            botServiceDelegate.sendMessage(chatId, "Тема или преподаватель не выбраны. Повторите процесс.", ParseMode.NONE);
            botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_CONFIRMATION);
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "confirm_supervisor".equals(callbackData);
    }
}
