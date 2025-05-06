package ru.derendyaev.ideathesis_bot_service.mustache;

import lombok.Getter;

@Getter
public class UserContext {
    // Геттеры
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String userType;

    // Конструктор, геттеры и сеттеры
    public UserContext(String firstName, String lastName, String email, String phone, String userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
    }

}