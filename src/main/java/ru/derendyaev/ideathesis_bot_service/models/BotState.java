package ru.derendyaev.ideathesis_bot_service.models;

public enum BotState {
    START,
    AWAITING_CREDENTIALS,
    AWAITING_COMPETENCIES,
    AWAITING_DOMAIN,
    AWAITING_TOPIC_CONFIRMATION,
    SHOWING_SUGGESTIONS,
    COMPLETED
}