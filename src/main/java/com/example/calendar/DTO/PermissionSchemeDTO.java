package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionSchemeDTO {
    private Long id;
    private String name;
    private String description;
    private Long roleLevelId;
    private String roleLevelName;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
}
