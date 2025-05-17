package com.example.calendar.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @JsonProperty("username")
    private String username;
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
