package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Getter;

@Getter
public enum TopicStatus {
    DRAFT("Черновик"),
    PENDING("На рассмотрении"),
    APPROVED("Утверждена"),
    REJECTED("Отклонена"),
    NEEDS_REVISION("Требуется доработка"),
    REVISED("Доработана"),
    FINAL_APPROVED("Окончательно утверждена");

    private final String displayName;

    TopicStatus(String displayName) {
        this.displayName = displayName;
    }

}