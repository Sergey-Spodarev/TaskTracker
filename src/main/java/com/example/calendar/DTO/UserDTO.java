package com.example.calendar.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data //lombok
public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 1, max = 100)
    private String userName;

    private String password;

    @Email(message = "Почта является обязательной")
    @NotBlank
    private String email;
}
