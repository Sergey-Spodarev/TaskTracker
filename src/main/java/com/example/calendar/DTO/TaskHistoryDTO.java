package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskHistoryDTO {
    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;

    // Кто изменил
    private String changedByName;
    private Long changedById;

    // Информация о задаче
    private Long taskId;
    private String taskTitle;

    // Информация о проекте
    private Long projectId;
    private String projectName;

    // Информация об отделе (опционально)
    private Long departmentId;
    private String departmentName;
}