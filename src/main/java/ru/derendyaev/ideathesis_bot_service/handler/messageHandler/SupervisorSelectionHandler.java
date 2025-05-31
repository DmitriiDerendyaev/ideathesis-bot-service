package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.client.UsersServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.employee.EmployeeAllDto;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

import java.util.List;

@Slf4j
@Component
public class SupervisorSelectionHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final UsersServiceClient usersServiceClient;
    private final MessageUtils messageUtils;

    @Autowired
    public SupervisorSelectionHandler(@Lazy BotServiceDelegate botServiceDelegate, UsersServiceClient usersServiceClient, MessageUtils messageUtils) {
        this.botServiceDelegate = botServiceDelegate;
        this.usersServiceClient = usersServiceClient;
        this.messageUtils = messageUtils;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        usersServiceClient.searchEmployeesByFullName(text)
                .doOnNext(employees -> {
                    if (employees.isEmpty()) {
                        botServiceDelegate.sendMessageWithKeyboard(
                                chatId,
                                "Преподаватель не найден. Введите ФИО заново.",
                                ParseMode.NONE,
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(List.of(InlineKeyboardButton.builder().text("Повторить поиск").callbackData("retry_supervisor").build()))
                                        .build()
                        );
                    } else {
                        EmployeeAllDto employee = employees.get(0);
                        String employeeInfo = String.format(
                                "*Преподаватель:*\nФИО: %s\nДолжности: %s\nКафедры: %s",
                                messageUtils.escapeMarkdownV2(employee.getFullName()),
                                messageUtils.escapeMarkdownV2(employee.getPosition()),
                                messageUtils.escapeMarkdownV2(employee.getDepartment())
                        );
                        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                                .keyboardRow(List.of(
                                        InlineKeyboardButton.builder().text("Выбрать").callbackData("select_supervisor_" + employee.getGuid()).build(),
                                        InlineKeyboardButton.builder().text("Повторить поиск").callbackData("retry_supervisor").build()
                                ))
                                .build();
                        botServiceDelegate.sendMessageWithKeyboard(chatId, employeeInfo, ParseMode.MARKDOWN, keyboard);
                    }
                })
                .doOnError(ex -> {
                    log.error("Ошибка при поиске преподавателя: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка. Попробуйте снова.", ParseMode.NONE);
                })
                .subscribe();
    }
}