package com.example.calendar.DTO;

import com.example.calendar.model.PermissionScheme;
import lombok.Data;

@Data
public class SchemePermissionDTO {
    private Long id;
    private PermissionScheme scheme;

    private String permissionKey;
    private String description;

    private Integer minLevel;
    private Integer maxLevel;
}
