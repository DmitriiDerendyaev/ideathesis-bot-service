package ru.derendyaev.ideathesis_bot_service.dto.employee;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class EmployeeAllDto {
    private UUID guid;
    private String fullName;
    private String surname;
    private String email;
    private Date dateOfBirth;
    private List<EmployeeEmploymentAllDto> employeeEmployments; // Список занятостей

    // Собираем все должности
    public String getPosition() {
        if (employeeEmployments == null || employeeEmployments.isEmpty()) {
            return "Не указана";
        }
        return employeeEmployments.stream()
                .filter(emp -> emp.getJobTitle() != null)
                .map(emp -> emp.getJobTitle().getName())
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    // Собираем все кафедры
    public String getDepartment() {
        if (employeeEmployments == null || employeeEmployments.isEmpty()) {
            return "Не указана";
        }
        return employeeEmployments.stream()
                .filter(emp -> emp.getSubdivision() != null)
                .map(emp -> emp.getSubdivision().getName())
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }
}