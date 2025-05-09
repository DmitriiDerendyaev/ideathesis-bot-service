package ru.derendyaev.ideathesis_bot_service.models;

public enum BotState {
    START,
    AWAITING_CREDENTIALS,
    AWAITING_COMPETENCIES,
    AWAITING_DOMAIN,
    TOPIC_SELECTION,
    TOPIC_CONFIRMATION, // Подтверждение выбора темы
    AWAITING_SUPERVISOR // Ожидание ввода ФИО преподавателя
}