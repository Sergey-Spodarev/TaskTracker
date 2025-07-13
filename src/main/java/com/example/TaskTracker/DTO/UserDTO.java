package com.example.TaskTracker.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @Email
    @NotBlank
    @JsonProperty("username")
    private String email;

    @NotBlank
    private String password;

    private String role;
}
