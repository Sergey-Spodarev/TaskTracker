package com.example.calendar.DTO;

import com.example.calendar.model.SystemPermissionKey;
import lombok.Data;

@Data
public class SystemPermissionCatalogDTO {
    private SystemPermissionKey key;  // CREATE_TASK (связь с enum)
    private String name;        // "Создание задачи" (для отображения)
    private String description; // описание
    private String group;        // "Задачи"
    private String icon;         // "fa-plus"
    private boolean builtIn;     // true
}
