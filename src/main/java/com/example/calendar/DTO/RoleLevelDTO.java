package com.example.calendar.DTO;

import lombok.Data;

@Data
public class RoleLevelDTO {
    private Long id;
    private Integer level;
    private String levelName;
    private Long roleId;
    private String roleName;
}
