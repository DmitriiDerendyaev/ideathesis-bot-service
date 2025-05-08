package ru.derendyaev.ideathesis_bot_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequestDto {
    private String ulogin;
    private String upassword;
    private String clientType = "telegram";

    public AuthRequestDto(String login, String password) {
        this.ulogin = login;
        this.upassword = password;
    }
}