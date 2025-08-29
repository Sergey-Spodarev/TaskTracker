package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCommentDTO {
    private Long id;

    private String comment;

    private LocalDateTime createdAt;
}
