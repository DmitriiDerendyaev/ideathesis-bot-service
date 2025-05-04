package ru.derendyaev.ideathesis_bot_service.services;

import lombok.Data;
import java.util.List;

import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;

@Data
public class UserSessionData {
    private AuthResponse auth;
    private List<String> competencies;
    private List<String> domains;
}