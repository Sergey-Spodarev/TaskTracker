package com.example.TaskTracker.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TasksDTO {
    @NotNull
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String priority;

    private String assigneeEmail;
}
