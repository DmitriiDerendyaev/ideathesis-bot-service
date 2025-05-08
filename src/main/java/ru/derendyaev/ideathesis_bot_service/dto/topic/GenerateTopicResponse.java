package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Data;

import java.util.List;

@Data
public class GenerateTopicResponse {
    private List<GeneratedTopicDto> topics;
}
