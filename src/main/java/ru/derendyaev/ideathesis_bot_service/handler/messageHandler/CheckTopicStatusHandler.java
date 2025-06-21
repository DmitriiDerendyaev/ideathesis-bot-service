package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.dto.topic.StudentTopicSelectionDto;
import ru.derendyaev.ideathesis_bot_service.dto.topic.TopicCommentDto;
import ru.derendyaev.ideathesis_bot_service.handler.callbackData.CallbackHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckTopicStatusHandler implements CallbackHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;
    private final UsersServiceClient usersServiceClient;

    @Autowired
    public CheckTopicStatusHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, UsersServiceClient usersServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
        this.usersServiceClient = usersServiceClient;
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("check_status_");
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long topicId = Long.parseLong(data.split("_")[2]);
        long chatId = callbackQuery.getMessage().getChatId();
        String studentGuid = botServiceDelegate.getUserStateService().getSessionData(chatId).getAuth().getUser().getGuid();

        log.info("Handling check_status for topic ID: {} and student GUID: {}", topicId, studentGuid);

        topicServiceClient.getActiveTopicsForStudent(studentGuid)
                .doOnNext(activeTopics -> {
                    StudentTopicSelectionDto topic = activeTopics.stream()
                            .filter(t -> t.getTopic().getId().equals(topicId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Topic not found"));
                    botServiceDelegate.getUserStateService().getSessionData(chatId).setSelectedTopic(topic.getTopic());
                    log.info("Found topic: {} (status: {}, supervisor GUID: {})",
                            topic.getTopic().getTitle(), topic.getTopic().getStatus().getDisplayName(), topic.getSupervisorGuid());

                    topicServiceClient.getCommentsForTopic(topicId)
                            .doOnNext(comments -> {
                                List<TopicCommentDto> teacherComments = comments.stream()
                                        .filter(c -> "TEACHER".equals(c.getAuthorType()))
                                        .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                                        .collect(Collectors.toList());

                                String lastTeacherComment = teacherComments.isEmpty()
                                        ? "Нет комментариев от преподавателя"
                                        : "Последний комментарий преподавателя:\n" + teacherComments.get(0).getCommentText();

                                usersServiceClient.getEmployeeById(topic.getSupervisorGuid().toString())
                                        .doOnNext(employee -> {
                                            log.info("Successfully retrieved supervisor: {}", employee.getFullName());
                                            String message = String.format(
                                                    "Тема: %s\n- Статус: %s\n- Руководитель: %s\n- Описание: %s\n\n%s",
                                                    topic.getTopic().getTitle(),
                                                    topic.getTopic().getStatus(),
                                                    employee.getFullName(),
                                                    topic.getTopic().getDescription(),
                                                    lastTeacherComment
                                            );

                                            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                                            List<InlineKeyboardButton> row = new ArrayList<>();
                                            row.add(InlineKeyboardButton.builder()
                                                    .text("Написать преподавателю")
                                                    .callbackData("contact_supervisor_" + topicId)
                                                    .build());
                                            row.add(InlineKeyboardButton.builder()
                                                    .text("Отозвать заявку")
                                                    .callbackData("withdraw_" + topicId)
                                                    .build());
                                            row.add(InlineKeyboardButton.builder()
                                                    .text("Добавить комментарий")
                                                    .callbackData("add_comment_" + topicId)
                                                    .build());
                                            rows.add(row);

                                            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(rows).build();
                                            botServiceDelegate.sendMessageWithKeyboard(chatId, message, ParseMode.NONE, keyboard);
                                        })
                                        .doOnError(ex -> {
                                            log.error("Ошибка при получении данных преподавателя для GUID {}: {}: {}",
                                                    topic.getSupervisorGuid(), ex.getClass().getName(), ex.getMessage(), ex);
                                            botServiceDelegate.sendMessage(chatId, "Ошибка при получении данных преподавателя.", ParseMode.NONE);
                                        })
                                        .subscribe();
                            })
                            .doOnError(ex -> {
                                log.error("Ошибка при получении комментариев для темы с ID {}: {}: {}",
                                        topicId, ex.getClass().getName(), ex.getMessage(), ex);
                                botServiceDelegate.sendMessage(chatId, "Ошибка при получении комментариев.", ParseMode.NONE);
                            })
                            .subscribe();
                })
                .doOnError(ex -> {
                    log.error("Ошибка при получении темы с ID {}: {}: {}",
                            topicId, ex.getClass().getName(), ex.getMessage(), ex);
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при получении статуса темы.", ParseMode.NONE);
                })
                .subscribe();
    }
}