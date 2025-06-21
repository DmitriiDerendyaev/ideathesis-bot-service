package ru.derendyaev.ideathesis_bot_service.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCommentRequest {

    @NotBlank
    private String commentText;
    private Long parentCommentId;
}

