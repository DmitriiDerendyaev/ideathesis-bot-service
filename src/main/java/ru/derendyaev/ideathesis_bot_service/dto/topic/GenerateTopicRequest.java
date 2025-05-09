package ru.derendyaev.ideathesis_bot_service.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateTopicRequest {
    @NotBlank(message = "Компетенции не могут быть пустыми")
    private String competencies;

    @NotBlank(message = "Область обучения не может быть пустой")
    private String areaOfStudy;

    @NotBlank(message = "Уровень обучения не может быть пустым")
    private String educationLevel;

    public GenerateTopicRequest(String competencies, String areaOfStudy, String educationLevel) {
        this.competencies = competencies;
        this.areaOfStudy = areaOfStudy;
        this.educationLevel = educationLevel;
    }

    public GenerateTopicRequest() {
    }
}
