package com.example.calendar.DTO;

import com.example.calendar.model.SystemRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 1, max = 100)
    private String userName;

    private String password;
    private SystemRole systemRole;
    private Long departmentId;
    private Long roleLevelId;
    private Integer roleLevel;

    @Email(message = "Почта является обязательной")
    @NotBlank
    private String email;
}
