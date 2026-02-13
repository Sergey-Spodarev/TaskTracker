package com.example.calendar.DTO;

import lombok.Data;

@Data
public class CreateRoleLevelDTO {
    private Long id;
    private Integer level;
    private String levelName;
    private Long roleId;
}
