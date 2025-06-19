package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.client.AuthServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.StudentTopicSelectionDto;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.mustache.UserContext;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.mustache.MustacheTemplateService;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class CredentialsMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final AuthServiceClient authServiceClient;
    private final MustacheTemplateService templateService;
    private final UsersServiceClient usersServiceClient;
    private final TopicServiceClient topicServiceClient;

    @Autowired
    public CredentialsMessageHandler(
            @Lazy BotServiceDelegate botServiceDelegate,
            AuthServiceClient authServiceClient,
            UsersServiceClient usersServiceClient,
            TopicServiceClient topicServiceClient,
            MustacheTemplateService templateService
    ) {
        this.botServiceDelegate = botServiceDelegate;
        this.authServiceClient = authServiceClient;
        this.usersServiceClient = usersServiceClient;
        this.topicServiceClient = topicServiceClient;
        this.templateService = templateService;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            botServiceDelegate.sendMessage(chatId, "Неверный формат. Введите: логин пароль", ParseMode.NONE);
            return;
        }

        authServiceClient.authenticateV2(parts[0], parts[1]) //TODO: switched to selfAuth authenticate -> authenticateV2
                .doOnNext(resp -> {
                    log.info("Authenticated: {}", resp.getUser());
                    botServiceDelegate.getUserStateService().saveAuth(chatId, resp);
                })
                .doOnError(UnauthorizedException.class, ex -> {
                    log.warn("Неверные учетные данные: {}", parts[0]);
                    botServiceDelegate.sendMessage(chatId, "Неверный логин или пароль. Попробуйте снова:", ParseMode.NONE);
                })
                .doOnError(ServiceUnavailableException.class, ex -> {
                    log.error("Сервис авторизации недоступен: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Сервис временно недоступен. Попробуйте позже.", ParseMode.NONE);
                })
                .block();

        String guid = botServiceDelegate.getUserStateService().getSessionData(chatId).getAuth().getUser().getGuid();
        log.info("Processing student with GUID: {}", guid);

        usersServiceClient.getStudentDetails(guid)
                .doOnNext(resp -> {
                    log.info("Student details: {}", resp.toString());
                    UserContext context = new UserContext(
                            resp.getFirstName(),
                            resp.getLastName(),
                            resp.getStudentGroup().getName().replace("-", "\\-"),
                            resp.getCourse().toString(),
                            resp.getDepartment().getName(),
                            resp.getDegreeLevel().getName()
                    );

                    String userInfoMessage = templateService.render("user_info.mustache", context);
                    botServiceDelegate.sendMessage(chatId, userInfoMessage, ParseMode.MARKDOWN);

                    topicServiceClient.getActiveTopicsForStudent(guid)
                            .doOnNext(activeTopics -> {
                                log.info("Number of active topics: {}", activeTopics.size());
                                if (activeTopics.size() > 1) {
                                    log.error("Unexpected number of active topics: {}. Expected at most one.", activeTopics.size());
                                }
                                if (!activeTopics.isEmpty()) {
                                    StudentTopicSelectionDto activeTopic = activeTopics.get(0);
                                    log.info("Found active topic: {} (status: {}, supervisor GUID: {})",
                                            activeTopic.getTopic().getTitle(),
                                            activeTopic.getTopic().getStatus(),
                                            activeTopic.getSupervisorGuid());

                                    StringBuilder message = new StringBuilder("У вас есть активная тема:\n");
                                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                                    List<InlineKeyboardButton> row = new ArrayList<>();

                                    usersServiceClient.getEmployeeById(activeTopic.getSupervisorGuid().toString())
                                            .doOnNext(employee -> {
                                                String supervisorName = employee.getFullName();
                                                log.info("Successfully retrieved supervisor: {}", supervisorName);
                                                message.append(String.format("- %s\n- Номер заявки: %s\n\n",
                                                        activeTopic.getTopic().getTitle(),
                                                        activeTopic.getTopic().getId()));
                                                row.add(InlineKeyboardButton.builder()
                                                        .text("Узнать статус")
                                                        .callbackData("check_status_" + activeTopic.getTopic().getId())
                                                        .build());
                                                row.add(InlineKeyboardButton.builder()
                                                        .text("Отозвать заявку")
                                                        .callbackData("withdraw_" + activeTopic.getTopic().getId())
                                                        .build());
                                                rows.add(row);

                                                message.append("Выберите действие для темы:");
                                                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(rows).build();
                                                botServiceDelegate.sendMessageWithKeyboard(chatId, message.toString(), ParseMode.NONE, keyboard);
                                                botServiceDelegate.getUserStateService().setState(chatId, BotState.PENDING_TOPICS);
                                            })
                                            .doOnError(ex -> {
                                                log.error("Ошибка при получении данных преподавателя для GUID {}: {}: {}",
                                                        activeTopic.getSupervisorGuid(), ex.getClass().getName(), ex.getMessage(), ex);
                                                botServiceDelegate.sendMessage(chatId, "Не удалось получить данные преподавателя. Попробуйте позже.", ParseMode.NONE);
                                                String competenciesMessage = templateService.render("request_competencies.mustache", context);
                                                botServiceDelegate.sendMessage(chatId, competenciesMessage, ParseMode.MARKDOWN);
                                                botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
                                            })
                                            .subscribe();
                                } else {
                                    log.info("No active topics found for student GUID: {}", guid);
                                    String competenciesMessage = templateService.render("request_competencies.mustache", context);
                                    botServiceDelegate.sendMessage(chatId, competenciesMessage, ParseMode.MARKDOWN);
                                    botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
                                }
                            })
                            .doOnError(ex -> {
                                log.error("Ошибка при получении активных тем для GUID {}: {}: {}",
                                        guid, ex.getClass().getName(), ex.getMessage(), ex);
                                String competenciesMessage = templateService.render("request_competencies.mustache", context);
                                botServiceDelegate.sendMessage(chatId, competenciesMessage, ParseMode.MARKDOWN);
                                botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
                            })
                            .subscribe();
                })
                .doOnError(UnauthorizedException.class, ex -> {
                    log.warn("Получены некорректные данные для GUID {}: {}", guid, ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Были введены некорректные данные.", ParseMode.NONE);
                })
                .doOnError(ServiceUnavailableException.class, ex -> {
                    log.error("Сервис пользователей недоступен: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Сервис временно недоступен. Попробуйте позже.", ParseMode.NONE);
                })
                .subscribe();
    }
}