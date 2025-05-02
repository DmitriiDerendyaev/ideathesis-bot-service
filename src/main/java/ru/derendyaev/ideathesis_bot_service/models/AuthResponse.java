package ru.derendyaev.ideathesis_bot_service.models;

import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;
    private UserData userData;
}

