package ru.derendyaev.ideathesis_bot_service.models;

import lombok.Getter;

@Getter
public enum ParseMode {
    MARKDOWN("MarkdownV2"),
    HTML("html"),
    NONE(null);

    private final String value;

    ParseMode(String value) {
        this.value = value;
    }

}

