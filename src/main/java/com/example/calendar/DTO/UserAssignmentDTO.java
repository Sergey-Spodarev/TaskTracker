package com.example.calendar.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAssignmentDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Role code cannot be blank")
    private String roleCode;

    @NotBlank(message = "Department name cannot be blank")
    private String departmentName;

    @Min(value = 0, message = "Department level cannot be negative")
    private Integer departmentLevel;

    private Long roleLevelId;
}
