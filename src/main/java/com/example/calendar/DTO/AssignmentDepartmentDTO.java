package com.example.calendar.DTO;

import lombok.Data;

@Data
public class AssignmentDepartmentDTO {
    private Long id;
    private String roleCode;           // например, "TEAM_LEAD"
    private String roleName;           // "Руководитель"
    private String sourceDepartment;   // "IT"
    private String targetDepartment;   // "QA"
    private Integer level;
    private boolean allowed;
}