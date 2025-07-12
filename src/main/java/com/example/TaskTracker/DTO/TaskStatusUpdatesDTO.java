package com.example.TaskTracker.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskStatusUpdatesDTO {
    private String status;
    private String comment;
    private LocalDateTime updatedAt;
}
