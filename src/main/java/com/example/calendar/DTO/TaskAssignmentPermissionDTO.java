package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskAssignmentPermissionDTO {
    public Long id;
    private boolean allowed;

    private Long roleLevelId;
    private Long departmentId;

    private String roleLevelName;
    private String departmentName;

    public LocalDateTime createdAt;
}
