package ru.derendyaev.ideathesis_bot_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class AuthRequest {
    private String login;
    private String password;
    private String clientType = "telegram";

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}