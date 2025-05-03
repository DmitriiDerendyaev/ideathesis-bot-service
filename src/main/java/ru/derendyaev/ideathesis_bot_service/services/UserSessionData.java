package ru.derendyaev.ideathesis_bot_service.services;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserSessionData {
    private List<String> competencies = new ArrayList<>();
    private List<String> domains = new ArrayList<>();
    private String preferredTopic;
    private List<String> suggestedTopics = new ArrayList<>();
    private Integer currentSuggestionIndex = 0;
}