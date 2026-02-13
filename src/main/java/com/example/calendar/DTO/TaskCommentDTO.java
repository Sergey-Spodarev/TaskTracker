package com.example.calendar.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskCommentDTO {
    private Long id;

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 5000, message = "Комментарий слишком длинный")
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long authorId;
    private String authorName;
}