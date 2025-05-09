package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Data;

@Data
public class SelectTopicRequest {
    private Long topicId;
    private String supervisorGuid; // Добавляем GUID преподавателя
}