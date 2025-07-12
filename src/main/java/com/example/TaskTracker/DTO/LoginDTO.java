package com.example.TaskTracker.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    private Long id;

    @Email
    @NotBlank
    private String email;

    private String role;
}
