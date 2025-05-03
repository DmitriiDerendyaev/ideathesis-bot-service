package ru.derendyaev.ideathesis_bot_service.services;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;

import java.util.List;

@Data
public class UserSessionData {
    private AuthResponse auth;
    private List<String> competencies;
    private List<String> domains;
}
