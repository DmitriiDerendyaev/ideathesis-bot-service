package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TopicCommentDto {
    private Long id;
    private Long topicId;
    private String authorType;
    private UUID authorGuid;
    private String commentText;
    private LocalDateTime createdAt;
    private Long parentCommentId;
}