package ru.derendyaev.ideathesis_bot_service.models.user;

import lombok.Data;

@Data
public class User {
    private String guid;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String userType;

    // Getters and setters
}