package ru.derendyaev.ideathesis_bot_service.dto.topic;

import lombok.Data;

@Data
public class GeneratedTopicDto {
    private Long id;
    private String title;
    private String description;
    private String actuality;
    private String problems;
    private String[] recommendedSkills;
    private TopicStatus status;

    public GeneratedTopicDto(Long id, String title, String description, String actuality, String problems, String[] recommendedSkills) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.actuality = actuality;
        this.problems = problems;
        this.recommendedSkills = recommendedSkills;
    }

    public GeneratedTopicDto(Long id, String title, String description, String actuality, String problems, String[] recommendedSkills, TopicStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.actuality = actuality;
        this.problems = problems;
        this.recommendedSkills = recommendedSkills;
        this.status = status;
    }

    public GeneratedTopicDto() {
    }
}