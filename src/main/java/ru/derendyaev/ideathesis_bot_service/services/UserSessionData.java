package ru.derendyaev.ideathesis_bot_service.services;

import lombok.Data;
import java.util.List;

import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;
import ru.derendyaev.ideathesis_bot_service.models.StudentDetails;

@Data
public class UserSessionData {
    private AuthResponse auth;
    private StudentDetails studentDetails; // Добавлено
    private List<String> competencies;
    private List<String> domains;
}