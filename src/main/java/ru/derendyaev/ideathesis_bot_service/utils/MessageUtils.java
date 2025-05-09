package ru.derendyaev.ideathesis_bot_service.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicResponse;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageUtils {

    /**
     * Escapes special characters for Telegram MarkdownV2.
     * @param text The input text to escape.
     * @return The escaped text.
     */
    public String escapeMarkdownV2(String text) {
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

    /**
     * Formats the problems section to ensure proper MarkdownV2 list formatting.
     * Preserves existing dashes (escaped or not) without adding duplicates.
     * @param problems The problems text to format.
     * @return The formatted problems text.
     */
    public String formatProblems(String problems) {
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
                    formatted.append(trimmedLine).append("\n");
                } else {
                    formatted.append("\\- ").append(trimmedLine).append("\n");
                }
            }
        }
        return formatted.length() > 0 ? formatted.toString() : "\\- Нет данных\n";
    }

    /**
     * Creates a GenerateTopicRequest based on the session data.
     * @param session The user session data containing competencies and domains.
     * @return The populated GenerateTopicRequest.
     */
    public GenerateTopicRequest createGenerateTopicRequest(UserSessionData session) {
        GenerateTopicRequest request = new GenerateTopicRequest();
        request.setCompetencies(String.join(",", session.getCompetencies()));
        request.setAreaOfStudy(String.join(",", session.getDomains()));
        request.setEducationLevel("BACHELOR"); // Значение по умолчанию
        return request;
    }

    /**
     * Builds the summary message to display competencies and domains.
     * @param session The user session data containing competencies and domains.
     * @return The formatted summary message.
     */
    public String buildSummaryMessage(UserSessionData session) {
        return String.format("Компетенции: %s\nОбласти: %s\nСпасибо! Подбираю темы...",
                String.join(", ", session.getCompetencies()),
                String.join(", ", session.getDomains()));
    }

    /**
     * Builds the message containing the generated topics.
     * @param response The GenerateTopicResponse containing the topics.
     * @return The formatted topics message in MarkdownV2.
     */
    public String buildTopicsMessage(GenerateTopicResponse response) {
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
        return topicsMessage.toString();
    }

    /**
     * Creates an InlineKeyboardMarkup with buttons for topic selection and regeneration.
     * @param response The GenerateTopicResponse containing the topics.
     * @return The InlineKeyboardMarkup with buttons.
     */
    public InlineKeyboardMarkup createTopicSelectionKeyboard(GenerateTopicResponse response) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Кнопки для выбора тем (максимум 3 темы)
        int topicIndex = 1;
        for (var topic : response.getTopics().subList(0, Math.min(3, response.getTopics().size()))) {
            InlineKeyboardButton topicButton = InlineKeyboardButton.builder()
                    .text("Тема " + topicIndex + ": " + topic.getTitle())
                    .callbackData("select_" + topic.getId())
                    .build();
            keyboard.add(List.of(topicButton));
            topicIndex++;
        }

        // Широкая кнопка "Перегенерировать"
        InlineKeyboardButton regenerateButton = InlineKeyboardButton.builder()
                .text("Перегенерировать")
                .callbackData("regenerate")
                .build();
        keyboard.add(List.of(regenerateButton));

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }
}
