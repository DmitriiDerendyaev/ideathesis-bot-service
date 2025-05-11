package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GeneratedTopicDto;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
@CallbackData("confirm_select_")
public class ConfirmTopicCallbackHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;

    @Autowired
    public ConfirmTopicCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate) {
        this.botServiceDelegate = botServiceDelegate;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        Long topicId = Long.parseLong(data.split("_")[2]);
        GeneratedTopicDto selectedTopic = session.getGeneratedTopics().getTopics().stream()
                .filter(t -> t.getId().equals(topicId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
        session.setSelectedTopic(selectedTopic);

        botServiceDelegate.sendMessage(chatId, "Тема готова к согласованию. Пожалуйста, выберите преподавателя, введя его ФИО.", ParseMode.NONE);
        botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_SUPERVISOR);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("confirm_select_");
    }
}
