package com.example.calendar.DTO;

import lombok.Data;

@Data
public class TaskHistoryDTO {
    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
}
