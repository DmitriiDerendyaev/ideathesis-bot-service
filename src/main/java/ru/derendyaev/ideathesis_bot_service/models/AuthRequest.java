package ru.derendyaev.ideathesis_bot_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class AuthRequest {
    private String ulogin;
    private String upassword;
    private String clientType = "telegram";

    public AuthRequest(String login, String password) {
        this.ulogin = login;
        this.upassword = password;
    }
}