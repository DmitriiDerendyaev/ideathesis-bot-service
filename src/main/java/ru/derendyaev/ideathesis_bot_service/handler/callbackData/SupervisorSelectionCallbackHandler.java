package ru.derendyaev.ideathesis_bot_service.handler.callbackData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

import java.util.List;

@Slf4j
@Component
@CallbackData("select_supervisor_")
public class SupervisorSelectionCallbackHandler implements CallbackHandler {
    private final BotServiceDelegate botServiceDelegate;
    private final UsersServiceClient usersServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public SupervisorSelectionCallbackHandler(@Lazy BotServiceDelegate botServiceDelegate, UsersServiceClient usersServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.usersServiceClient = usersServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);

        String supervisorGuid = data.split("_")[2];
        usersServiceClient.getEmployeeById(supervisorGuid)
                .doOnNext(employee -> {
                    session.setSupervisor(employee);
                    String confirmationMessage = String.format(
                            "*Выбранный преподаватель:*\nФИО: %s\nДолжность: %s\nКафедра: %s\n\nПодтвердите выбор преподавателя\\.",
                            messageUtils.escapeMarkdownV2(employee.getFullName()),
                            messageUtils.escapeMarkdownV2(employee.getPosition()),
                            messageUtils.escapeMarkdownV2(employee.getDepartment())
                    );
                    InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                            .keyboardRow(List.of(
                                    InlineKeyboardButton.builder().text("Подтвердить").callbackData("confirm_supervisor").build(),
                                    InlineKeyboardButton.builder().text("Отмена").callbackData("retry_supervisor").build()
                            ))
                            .build();
                    botServiceDelegate.sendMessageWithKeyboard(chatId, confirmationMessage, ParseMode.MARKDOWN, keyboard);
                    botServiceDelegate.getUserStateService().setState(chatId, BotState.SUPERVISOR_CONFIRMATION);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при получении данных преподавателя: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
                })
                .subscribe();
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("select_supervisor_");
    }
}
