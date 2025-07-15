package com.example.TaskTracker.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TasksUserDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String priority;
    private String status;
    private String comment;
    private LocalDate updatedAt;
    private String assigneeEmail;
}
