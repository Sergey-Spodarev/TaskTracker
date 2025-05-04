package com.example.calendar.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    @NotBlank(message = "Название задачи обязательно")
    private String title;

    @NotNull(message = "Время начало для задачи обязательно")
    private LocalDateTime start;

    @NotNull(message = "Время конца выполнения задачи обязательно")
    private LocalDateTime end;

    private boolean allDay;
}
