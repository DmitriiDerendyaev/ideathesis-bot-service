package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;

@Slf4j
@Component
public class DomainMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;

    @Autowired
    public DomainMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
    }

    /**
     * Escapes special characters for Telegram MarkdownV2.
     * @param text The input text to escape.
     * @return The escaped text.
     */
    private String escapeMarkdownV2(String text) {
        if (text == null) {
            return "";
        }
        String[] specialCharacters = new String[]{
                "\\_", "\\*", "\\[", "\\]", "\\(", "\\)", "\\~", "\\`", "\\>",
                "\\#", "\\+", "\\-", "\\=", "\\|", "\\{", "\\}", "\\.", "\\!"
        };
        String escapedText = text;
        for (String specialChar : specialCharacters) {
            String rawChar = specialChar.substring(1); // Remove the leading \
            escapedText = escapedText.replace(rawChar, specialChar);
        }
        return escapedText;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        session.setDomains(botServiceDelegate.splitList(text));

        String studentGuid = session.getAuth().getUser().getGuid();
        String competencies = String.join(",", session.getCompetencies());
        String areaOfStudy = String.join(",", session.getDomains());
        String educationLevel = "BACHELOR";

        GenerateTopicRequest request = new GenerateTopicRequest();
        request.setCompetencies(competencies);
        request.setAreaOfStudy(areaOfStudy);
        request.setEducationLevel(educationLevel);

        botServiceDelegate.sendMessage(chatId,
                String.format("Компетенции: %s\nОбласти: %s\nСпасибо! Подбираю темы...",
                        String.join(", ", session.getCompetencies()),
                        String.join(", ", session.getDomains())), ParseMode.HTML);

        topicServiceClient.generateTopics(studentGuid, request)
                .doOnNext(response -> {
                    StringBuilder topicsMessage = new StringBuilder("*Сгенерированные темы:*\n\n");
                    response.getTopics().forEach(topic -> {
                        topicsMessage.append(String.format(
                                "📌 *%s*\n" +
                                        "*Описание:* %s\n" +
                                        "*Актуальность:* %s\n" +
                                        "*Проблемы:*\n%s\n" +
                                        "*Рекомендуемые навыки:* %s\n\n",
                                escapeMarkdownV2(topic.getTitle()),
                                escapeMarkdownV2(topic.getDescription()),
                                escapeMarkdownV2(topic.getActuality()),
                                formatProblems(escapeMarkdownV2(topic.getProblems())),
                                escapeMarkdownV2(String.join(", ", topic.getRecommendedSkills()))
                        ));
                    });
                    botServiceDelegate.sendMessage(chatId, topicsMessage.toString(), ParseMode.MARKDOWN);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
                })
                .doOnSuccess(response -> botServiceDelegate.getUserStateService().clear(chatId))
                .subscribe();
    }

    /**
     * Formats the problems section to ensure proper MarkdownV2 list formatting.
     * Preserves existing dashes (escaped or not) without adding duplicates.
     * @param problems The problems text to format.
     * @return The formatted problems text.
     */
    private String formatProblems(String problems) {
        if (problems == null || problems.trim().isEmpty()) {
            return "\\- Нет данных\n";
        }
        String[] problemLines = problems.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (String line : problemLines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                // Check if the line starts with an escaped dash (\-) or a raw dash (-)
                if (trimmedLine.startsWith("\\-") || trimmedLine.startsWith("-")) {
                    // Use the line as-is (preserving the escaped dash)
                    formatted.append(trimmedLine).append("\n");
                } else {
                    // Add an escaped dash prefix for non-dashed lines
                    formatted.append("\\- ").append(trimmedLine).append("\n");
                }
            }
        }
        return formatted.length() > 0 ? formatted.toString() : "\\- Нет данных\n";
    }
}