package ru.derendyaev.ideathesis_bot_service.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private User user;
    private String message;

    // Getters and setters
}