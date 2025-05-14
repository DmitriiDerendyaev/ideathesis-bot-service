package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Data;

import java.util.UUID;

@Data
public class PendingTopicSelectionDto {
    private GeneratedTopicDto topic;
    private UUID studentGuid;
    private UUID supervisorGuid;
}


