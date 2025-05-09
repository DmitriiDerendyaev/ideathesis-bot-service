package ru.derendyaev.ideathesis_bot_service.models.user;

import lombok.Data;
import java.util.List;

import ru.derendyaev.ideathesis_bot_service.dto.auth.AuthResponse;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GeneratedTopicDto;
import ru.derendyaev.ideathesis_bot_service.models.student.StudentDetails;

@Data
public class UserSessionData {
    private AuthResponse auth;
    private StudentDetails studentDetails;
    private List<String> competencies;
    private List<String> domains;
    private List<GeneratedTopicDto> generatedTopics; // Добавлено для хранения тем
}