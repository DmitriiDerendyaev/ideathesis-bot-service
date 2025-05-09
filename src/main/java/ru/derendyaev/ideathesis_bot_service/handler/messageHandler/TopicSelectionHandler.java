package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.handler.CallbackHandler;
import ru.derendyaev.ideathesis_bot_service.models.BotState;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import ru.derendyaev.ideathesis_bot_service.utils.MessageUtils;

import java.util.List;


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
            // Показываем предыдущие компетенции, если они есть
            String previousCompetencies = String.join(", ", session.getCompetencies());
            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(
                            InlineKeyboardButton.builder()
                                    .text("Использовать предыдущие: " + previousCompetencies)
                                    .callbackData("use_previous_competencies")
                                    .build()
                    ))
                    .build();

            botServiceDelegate.sendMessageWithKeyboard(
                    chatId,
                    "Введите компетенции через запятую.",
                    ParseMode.NONE,
                    keyboard
            );
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_COMPETENCIES);
        } else if ("use_previous_competencies".equals(data)) {
            // Используем предыдущие компетенции и переходим к вводу доменов
            String previousDomains = String.join(", ", session.getDomains());
            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(
                            InlineKeyboardButton.builder()
                                    .text("Использовать предыдущие: " + previousDomains)
                                    .callbackData("use_previous_domains")
                                    .build()
                    ))
                    .build();

            botServiceDelegate.sendMessageWithKeyboard(
                    chatId,
                    "Введите области интересов через запятую.",
                    ParseMode.NONE,
                    keyboard
            );
            botServiceDelegate.getUserStateService().setState(chatId, BotState.AWAITING_DOMAIN);
        } else if ("use_previous_domains".equals(data)) {
            // Используем предыдущие домены и переходим к генерации тем
            handleDomainInput(chatId, session);
        } else if (data.startsWith("select_")) {
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

    private void handleDomainInput(long chatId, UserSessionData session) {
        String studentGuid = session.getAuth().getUser().getGuid();
        botServiceDelegate.sendMessage(chatId, "Подбираю темы... ⏳", ParseMode.NONE);

        topicServiceClient.generateTopics(studentGuid, new GenerateTopicRequest(
                        String.join(",", session.getCompetencies()),
                        String.join(",", session.getDomains()),
                        "BACHELOR"
                ))
                .doOnNext(response -> {
                    session.setGeneratedTopics(response);
                    String topicsMessage = new MessageUtils().buildTopicsMessage(response);
                    botServiceDelegate.sendMessageWithKeyboard(
                            chatId,
                            topicsMessage,
                            ParseMode.MARKDOWN,
                            new MessageUtils().createTopicSelectionKeyboard(response)
                    );
                })
                .doOnError(ex -> {
                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
                })
                .doOnSuccess(response -> botServiceDelegate.getUserStateService().setState(chatId, BotState.TOPIC_SELECTION))
                .subscribe();
    }
}