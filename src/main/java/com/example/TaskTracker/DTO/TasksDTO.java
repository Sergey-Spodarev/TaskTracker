package com.example.TaskTracker.DTO;

import com.example.TaskTracker.model.Users;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TasksDTO {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String priority;

    private String assigneeEmail;
}
