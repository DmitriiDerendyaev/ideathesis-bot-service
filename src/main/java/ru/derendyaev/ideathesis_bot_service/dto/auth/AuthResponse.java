package ru.derendyaev.ideathesis_bot_service.dto.auth;

import lombok.Data;
import ru.derendyaev.ideathesis_bot_service.models.user.User;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private User user;
    private String message;

    // Getters and setters
}