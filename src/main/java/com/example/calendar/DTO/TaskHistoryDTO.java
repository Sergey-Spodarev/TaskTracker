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
    private String changedByName;
    private Long changedById;
}
