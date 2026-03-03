package com.example.calendar.DTO;

import com.example.calendar.model.SystemPermissionCatalog;
import lombok.Data;

import java.util.Set;

@Data
public class SystemPermissionDefinitionDTO {
    private Long id;
    private Set<SystemPermissionCatalog> systemPermissionCatalog;
    private String code;
    private String name;
    private String description;
}
