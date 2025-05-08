package ru.derendyaev.ideathesis_bot_service.models.user;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.derendyaev.ideathesis_bot_service.dto.auth.AuthResponse;
import ru.derendyaev.ideathesis_bot_service.models.BotState;

import java.util.concurrent.ConcurrentHashMap;

@Data
@Service
public class UserStateService {
    private final ConcurrentHashMap<Long, BotState> states = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, UserSessionData> sessions = new ConcurrentHashMap<>();

    public BotState getState(long chatId) {
        return states.getOrDefault(chatId, BotState.START);
    }

    public void setState(long chatId, BotState state) {
        states.put(chatId, state);
    }

    public void saveAuth(long chatId, AuthResponse resp) {
        sessions.computeIfAbsent(chatId, id -> new UserSessionData()).setAuth(resp);
    }

    public UserSessionData getSessionData(long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSessionData());
    }

    public void clear(long chatId) {
        states.remove(chatId);
        sessions.remove(chatId);
    }
}