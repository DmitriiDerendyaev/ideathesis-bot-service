package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.AddCommentRequest;
import ru.derendyaev.ideathesis_bot_service.dto.topic.TopicStatus;
import ru.derendyaev.ideathesis_bot_service.dto.topic.TopicStatusUpdateRequest;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.models.user.UserStateService;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

@Slf4j
@Component
public class AddCommentMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final UserStateService stateService;

    @Autowired
    public AddCommentMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, UserStateService stateService) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.stateService = stateService;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String commentText = msg.getText();
        UserSessionData sessionData = stateService.getSessionData(chatId);
        Long topicId = sessionData.getSelectedTopic().getId();
        String studentGuid = sessionData.getAuth().getUser().getGuid();

        AddCommentRequest addRequest = new AddCommentRequest();
        addRequest.setCommentText(commentText);

        topicServiceClient.addComment(topicId, studentGuid, addRequest)
                .flatMap(commentDto -> {
                    TopicStatusUpdateRequest statusRequest = new TopicStatusUpdateRequest();
                    statusRequest.setTopicId(topicId);
                    statusRequest.setStatus(TopicStatus.PENDING);
                    return topicServiceClient.updateTopicStatus(studentGuid, statusRequest)
                            .then(Mono.just(commentDto));
                })
                .doOnSuccess(commentDto -> {
                    botServiceDelegate.sendMessage(chatId, "Комментарий успешно добавлен.", ParseMode.NONE);
                    stateService.setState(chatId, BotState.START);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при добавлении комментария или обновлении статуса: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Ошибка при добавлении комментария или обновлении статуса.", ParseMode.NONE);
                    stateService.setState(chatId, BotState.START);
                })
                .subscribe();
    }
}