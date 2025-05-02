package ru.derendyaev.ideathesis_bot_service.services;

import org.springframework.stereotype.Service;
import ru.derendyaev.ideathesis_bot_service.models.AuthResponse;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    private final ConcurrentHashMap<Long, Boolean> awaitingCredentials = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Boolean> firstAuth = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AuthResponse> authData = new ConcurrentHashMap<>();

    public void setAwaitingCredentials(Long chatId, boolean state) {
        awaitingCredentials.put(chatId, state);
    }

    public boolean isAwaitingCredentials(Long chatId) {
        return awaitingCredentials.getOrDefault(chatId, false);
    }

    public void setFirstAuth(Long chatId, boolean state) {
        firstAuth.put(chatId, state);
    }

    public boolean isFirstAuth(Long chatId) {
        return firstAuth.getOrDefault(chatId, true);
    }

    public void saveAuthData(Long chatId, AuthResponse response) {
        authData.put(chatId, response);
    }

    public AuthResponse getAuthData(Long chatId) {
        return authData.get(chatId);
    }
}
