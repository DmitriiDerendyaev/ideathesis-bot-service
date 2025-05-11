package ru.derendyaev.ideathesis_bot_service.dto.employee;

import lombok.Data;

import java.util.UUID;

@Data
public class EmployeeEmploymentAllDto {
    private Long id;
    private UUID employeeGuid;
    private JobTitleDto jobTitle;
    private StaffCategoryDto staffCategory;
    private EmploymentTypeDto employmentType;
    private SubdivisionDto subdivision;
    private String jobState;
}

@Data
class JobTitleDto {
    private String name; // Предполагаемое поле для названия должности
}

@Data
class StaffCategoryDto {
    private String name; // Предполагаемое поле для категории
}

@Data
class EmploymentTypeDto {
    private String name; // Предполагаемое поле для типа занятости
}

@Data
class SubdivisionDto {
    private String name; // Предполагаемое поле для названия подразделения (кафедры)
}
