package com.example.calendar.DTO;

import com.example.calendar.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    @NotBlank(message = "Название задачи обязательно")
    private String title;

    @NotNull(message = "Время начало для задачи обязательно")
    private LocalDateTime start;

    @NotNull(message = "Время конца выполнения задачи обязательно")
    private LocalDateTime end;

    private TaskStatus status;

    private Long assigneeId;
    private Long projectId;
}
