package ru.derendyaev.ideathesis_bot_service.dto.topic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicStatusUpdateRequest {
    @NotNull(message = "ID темы не может быть пустым")
    private Long topicId;
    @NotNull(message = "Статус не может быть пустым")
    private TopicStatus status;
}

