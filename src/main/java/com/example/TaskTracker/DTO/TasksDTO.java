package com.example.TaskTracker.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TasksDTO {
    private String title;
    private String description;
    private LocalDate deadline;
    private String priority;
    private LocalDate createdAt;
}
