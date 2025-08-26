package com.example.calendar.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectDTO {
    private Long id;
    @NotBlank(message = "Имя проекта обязательно")
    private String name;
    @NotBlank(message = "Ключ проекта обязателен")
    private String projectKey;
}
