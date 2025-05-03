package ru.derendyaev.ideathesis_bot_service.services;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;
import ru.derendyaev.ideathesis_bot_service.models.BotState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    private final ConcurrentHashMap<Long, BotState> userStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AuthResponse> authData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, UserSessionData> sessionData = new ConcurrentHashMap<>();

    public BotState getCurrentState(Long chatId) {
        return userStates.getOrDefault(chatId, BotState.START);
    }

    public void setState(Long chatId, BotState state) {
        userStates.put(chatId, state);
    }

    public void saveAuthData(Long chatId, AuthResponse response) {
        authData.put(chatId, response);
        sessionData.putIfAbsent(chatId, new UserSessionData());
    }

    public AuthResponse getAuthData(Long chatId) {
        return authData.get(chatId);
    }

    public UserSessionData getSessionData(Long chatId) {
        return sessionData.computeIfAbsent(chatId, k -> new UserSessionData());
    }

    public void clearSession(Long chatId) {
        userStates.remove(chatId);
        authData.remove(chatId);
        sessionData.remove(chatId);
    }
}