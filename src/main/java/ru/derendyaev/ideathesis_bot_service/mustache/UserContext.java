package ru.derendyaev.ideathesis_bot_service.mustache;

import lombok.Data;
import lombok.Getter;

@Data
public class UserContext {
    // Геттеры
    private String firstName;
    private String lastName;
    private String groupName;
    private String course;
    private String department;
    private String degreeLevel;

    public UserContext(String firstName, String lastName, String groupName, String course, String department, String degreeLevel) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.groupName = groupName;
        this.course = course;
        this.department = department;
        this.degreeLevel = degreeLevel;
    }
}