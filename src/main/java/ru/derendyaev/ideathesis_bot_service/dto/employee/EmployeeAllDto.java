package ru.derendyaev.ideathesis_bot_service.dto.employee;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class EmployeeAllDto {
    private UUID guid;
    private String fullName;
    private String surname;
    private String email;
    private Date dateOfBirth;
    private String position;
    private String department;
}
