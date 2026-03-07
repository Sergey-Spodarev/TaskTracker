package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomPermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createTime;
}
