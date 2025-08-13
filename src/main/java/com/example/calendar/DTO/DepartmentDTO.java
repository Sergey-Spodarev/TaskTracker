package com.example.calendar.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    @NotBlank
    private String name;
}
