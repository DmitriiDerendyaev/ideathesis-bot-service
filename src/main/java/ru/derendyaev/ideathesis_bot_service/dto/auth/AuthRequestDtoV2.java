package ru.derendyaev.ideathesis_bot_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequestDtoV2 {
    private String username;
    private String password;
    private String clientType = "telegram";

    public AuthRequestDtoV2(String login, String password) {
        this.username = login;
        this.password = password;
    }
}