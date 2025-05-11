//package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
//import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
//import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
//import ru.derendyaev.ideathesis_bot_service.dto.topic.GeneratedTopicDto;
//import ru.derendyaev.ideathesis_bot_service.dto.topic.TopicStatus;
//import ru.derendyaev.ideathesis_bot_service.dto.topic.TopicStatusUpdateRequest;
//import ru.derendyaev.ideathesis_bot_service.handler.CallbackHandler;
//import ru.derendyaev.ideathesis_bot_service.models.BotState;
//import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
//import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
//import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
//import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;
//
//import java.util.List;
//import java.util.stream.Collectors;
//@Slf4j
//@Component
//public class TopicSelectionHandler implements CallbackHandler {
//
//    private final BotServiceDelegate botServiceDelegate;
//    private final TopicServiceClient topicServiceClient;
//    private final UsersServiceClient usersServiceClient;
//    private final MessageUtils messageUtils;
//
//    public TopicSelectionHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient, UsersServiceClient usersServiceClient, MessageUtils messageUtils) {
//        this.botServiceDelegate = botServiceDelegate;
//        this.topicServiceClient = topicServiceClient;
//        this.usersServiceClient = usersServiceClient;
//        this.messageUtils = messageUtils;
//    }
//
//    @Override
//    public void handleCallback(CallbackQuery callbackQuery) {
//        String data = callbackQuery.getData();
//        long chatId = callbackQuery.getMessage().getChatId();
//        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
//
//        if ("regenerate".equals(data)) {
//            String previousCompetencies = String.join(", ", session.getCompetencies() != null ? session.getCompetencies() : List.of());
//            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
//                    .keyboardRow(List.of(
//                            InlineKeyboardButton.builder()
//                                    .text("Использовать предыдущие: " + previousCompetencies)
//                                    .callbackData("use_previous_competencies")
//                                    .build()
//                    ))
//                    .build();
//            botServiceDelegate.sendMessageWithKeyboard(
//                    chatId, "Введите компетенции через запятую.", ParseMode.NONE, keyboard
//            );
//            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
//        } else if ("use_previous_competencies".equals(data)) {
//            String previousDomains = String.join(", ", session.getDomains() != null ? session.getDomains() : List.of());
//            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
//                    .keyboardRow(List.of(
//                            InlineKeyboardButton.builder()
//                                    .text("Использовать предыдущие: " + previousDomains)
//                                    .callbackData("use_previous_domains")
//                                    .build()
//                    ))
//                    .build();
//            botServiceDelegate.sendMessageWithKeyboard(
//                    chatId, "Введите области интересов через запятую.", ParseMode.NONE, keyboard
//            );
//            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_DOMAIN);
//        } else if ("use_previous_domains".equals(data)) {
//            handleDomainInput(chatId, session);
//        } else if (data.startsWith("select_") && !data.startsWith("select_supervisor_")) {
//            // Обработка выбора темы (исключаем выбор преподавателя)
//            Long topicId = Long.parseLong(data.split("_")[1]);
//            String studentGuid = session.getAuth().getUser().getGuid();
//
//            topicServiceClient.getLastTenTopics(studentGuid)
//                    .doOnNext(lastTenTopics -> {
//                        GeneratedTopicDto selectedTopic = session.getGeneratedTopics().getTopics().stream()
//                                .filter(t -> t.getId().equals(topicId))
//                                .findFirst()
//                                .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
//                        session.setSelectedTopic(selectedTopic);
//
//                        String confirmationMessage = messageUtils.buildTopicConfirmationMessage(selectedTopic, lastTenTopics);
//                        InlineKeyboardMarkup keyboard = messageUtils.createConfirmationKeyboard(selectedTopic, lastTenTopics);
//                        botServiceDelegate.sendMessageWithKeyboard(
//                                chatId, confirmationMessage, ParseMode.MARKDOWN, keyboard
//                        );
//                        botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_CONFIRMATION);
//                    })
//                    .doOnError(ex -> {
//                        log.error("Ошибка при получении истории тем: {}", ex.getMessage());
//                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
//                    })
//                    .subscribe();
//        } else if (data.startsWith("select_supervisor_")) {
//            // Обработка выбора преподавателя
//            String supervisorGuid = data.split("_")[2];
//            usersServiceClient.getEmployeeById(supervisorGuid)
//                    .doOnNext(employee -> {
//                        session.setSupervisor(employee);
//                        String confirmationMessage = String.format(
//                                "*Выбранный преподаватель:*\nФИО: %s\nДолжность: %s\nКафедра: %s\n\nПодтвердите выбор преподавателя\\.",
//                                messageUtils.escapeMarkdownV2(employee.getFullName()),
//                                messageUtils.escapeMarkdownV2(employee.getPosition()),
//                                messageUtils.escapeMarkdownV2(employee.getDepartment())
//                        );
//                        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
//                                .keyboardRow(List.of(
//                                        InlineKeyboardButton.builder().text("Подтвердить").callbackData("confirm_supervisor").build(),
//                                        InlineKeyboardButton.builder().text("Отмена").callbackData("retry_supervisor").build()
//                                ))
//                                .build();
//                        botServiceDelegate.sendMessageWithKeyboard(chatId, confirmationMessage, ParseMode.MARKDOWN, keyboard);
//                        botServiceDelegate.getUserStateService().setState(chatId, BotState.SUPERVISOR_CONFIRMATION);
//                    })
//                    .doOnError(ex -> {
//                        log.error("Ошибка при получении данных преподавателя: {}", ex.getMessage());
//                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
//                    })
//                    .subscribe();
//        } else if (data.startsWith("change_select_")) {
//            Long topicId = Long.parseLong(data.split("_")[2]);
//            String studentGuid = session.getAuth().getUser().getGuid();
//
//            topicServiceClient.getLastTenTopics(studentGuid)
//                    .doOnNext(lastTenTopics -> {
//                        GeneratedTopicDto newTopic = lastTenTopics.stream()
//                                .filter(t -> t.getId().equals(topicId))
//                                .findFirst()
//                                .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
//                        session.setSelectedTopic(newTopic);
//
//                        String confirmationMessage = messageUtils.buildTopicConfirmationMessage(newTopic, lastTenTopics);
//                        InlineKeyboardMarkup keyboard = messageUtils.createConfirmationKeyboard(newTopic, lastTenTopics);
//                        botServiceDelegate.sendMessageWithKeyboard(
//                                chatId, confirmationMessage, ParseMode.MARKDOWN, keyboard
//                        );
//                    })
//                    .doOnError(ex -> {
//                        log.error("Ошибка при получении истории тем: {}", ex.getMessage());
//                        botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
//                    })
//                    .subscribe();
//        } else if (data.startsWith("confirm_select_")) {
//            Long topicId = Long.parseLong(data.split("_")[2]);
//            String studentGuid = session.getAuth().getUser().getGuid();
//
//            // Устанавливаем выбранную тему в сессию и переходим к выбору преподавателя
//            GeneratedTopicDto selectedTopic = session.getGeneratedTopics().getTopics().stream()
//                    .filter(t -> t.getId().equals(topicId))
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalStateException("Тема не найдена"));
//            session.setSelectedTopic(selectedTopic);
//
//            botServiceDelegate.sendMessage(chatId, "Тема готова к согласованию. Пожалуйста, выберите преподавателя, введя его ФИО.", ParseMode.NONE);
//            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_SUPERVISOR);
//        } else if ("cancel_select".equals(data)) {
//            botServiceDelegate.sendMessageWithKeyboard(
//                    chatId,
//                    messageUtils.buildTopicsMessage(session.getGeneratedTopics()),
//                    ParseMode.MARKDOWN,
//                    messageUtils.createTopicSelectionKeyboard(session.getGeneratedTopics())
//            );
//            botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_SELECTION);
//        } else if ("retry_supervisor".equals(data)) {
//            botServiceDelegate.sendMessage(chatId, "Введите ФИО преподавателя для выбора руководителя.", ParseMode.NONE);
//            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_SUPERVISOR);
//        } else if ("confirm_supervisor".equals(data)) {
//            if (session.getSupervisor() != null && session.getSelectedTopic() != null) {
//                String studentGuid = session.getAuth().getUser().getGuid();
//                String supervisorGuid = session.getSupervisor().getGuid().toString();
//
//                topicServiceClient.selectTopic(studentGuid, session.getSelectedTopic().getId(), supervisorGuid)
//                        .doOnSuccess(response -> {
//                            botServiceDelegate.sendMessage(chatId, "Тема успешно выбрана и отправлена на согласование преподавателю.\n\nОжидайте ответное сообщение!", ParseMode.NONE);
//                            botServiceDelegate.getUserStateService().setState(chatId, BotState.COMPLETED);
//                        })
//                        .doOnError(ex -> {
//                            log.error("Ошибка при выборе темы: {}", ex.getMessage());
//                            botServiceDelegate.sendMessage(chatId, "Произошла ошибка при выборе темы. Попробуйте снова.", ParseMode.NONE);
//                        })
//                        .subscribe();
//            } else {
//                botServiceDelegate.sendMessage(chatId, "Тема или преподаватель не выбраны. Повторите процесс.", ParseMode.NONE);
//                botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_CONFIRMATION);
//            }
//        }
//    }
//
//    private void handleDomainInput(long chatId, UserSessionData session) {
//        String studentGuid = session.getAuth().getUser().getGuid();
//        botServiceDelegate.sendMessage(chatId, "Подбираю темы... ⏳", ParseMode.NONE);
//
//        topicServiceClient.generateTopics(studentGuid, new GenerateTopicRequest(
//                        String.join(",", session.getCompetencies() != null ? session.getCompetencies() : List.of()),
//                        String.join(",", session.getDomains() != null ? session.getDomains() : List.of()),
//                        "BACHELOR"
//                ))
//                .doOnNext(response -> {
//                    session.setGeneratedTopics(response);
//                    String topicsMessage = messageUtils.buildTopicsMessage(response);
//                    botServiceDelegate.sendMessageWithKeyboard(
//                            chatId,
//                            topicsMessage,
//                            ParseMode.MARKDOWN,
//                            messageUtils.createTopicSelectionKeyboard(response)
//                    );
//                })
//                .doOnError(ex -> {
//                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
//                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
//                })
//                .doOnSuccess(response -> botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_SELECTION))
//                .subscribe();
//    }
//}