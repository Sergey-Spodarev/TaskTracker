package com.example.TaskTracker.DTO;

import lombok.Data;

@Data
public class StatusUpdateRequestDTO {
    private Long taskId;
    private String status;
    private String comment;
}
