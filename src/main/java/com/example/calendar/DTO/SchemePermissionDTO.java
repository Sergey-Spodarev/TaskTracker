package com.example.calendar.DTO;

import lombok.Data;

@Data
public class SchemePermissionDTO {
    private Long id;
    private String permissionKey;
    private String description;
    private Integer minLevel;
    private Integer maxLevel;
}
