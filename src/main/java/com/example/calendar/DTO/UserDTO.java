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

    @Pattern(//Позволяет задать собственный шаблон регулярного выражения. Здесь проверяется, чтобы пароль содержал как минимум 8 символов, включая буквы и цифры.
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Пароль: минимум 8 символов, буквы и цифры"
    )
    private String password;

    @Email(message = "Почта является обязательной")
    @NotBlank
    private String email;
}
