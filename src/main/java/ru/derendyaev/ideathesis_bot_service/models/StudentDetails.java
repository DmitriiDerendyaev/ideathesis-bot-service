package ru.derendyaev.ideathesis_bot_service.models;

import lombok.Data;

@Data
public class StudentDetails {
    private String guid;
    private String firstName;
    private String lastName;
    private Integer course;
    private StudentGroup studentGroup;
    private Department department;
    private DegreeLevel degreeLevel;
    // Добавьте другие поля из примера JSON
}

