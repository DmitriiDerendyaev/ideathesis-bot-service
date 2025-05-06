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

    @Override
    public String toString() {
        return "StudentDetails{" +
                "guid='" + guid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", course=" + course +
                ", studentGroup=" + studentGroup +
                ", department=" + department +
                ", degreeLevel=" + degreeLevel +
                '}';
    }
}

