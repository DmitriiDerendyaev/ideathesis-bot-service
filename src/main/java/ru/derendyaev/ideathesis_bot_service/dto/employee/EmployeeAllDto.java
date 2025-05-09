package ru.derendyaev.ideathesis_bot_service.dto.employee;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class EmployeeAllDto {
    private UUID guid;
    private String fullName;
    private String surname;
    private String email;
    private Date dateOfBirth;
    private List<EmployeeEmploymentAllDto> employeeEmployments; // Добавляем список занятостей

    // Динамическое получение должности и кафедры из первого элемента employeeEmployments
    public String getPosition() {
        return employeeEmployments != null && !employeeEmployments.isEmpty() ?
                employeeEmployments.get(0).getJobTitle() != null ? employeeEmployments.get(0).getJobTitle().getName() : null :
                null;
    }

    public String getDepartment() {
        return employeeEmployments != null && !employeeEmployments.isEmpty() ?
                employeeEmployments.get(0).getSubdivision() != null ? employeeEmployments.get(0).getSubdivision().getName() : null :
                null;
    }
}