package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "system_permission_catalog")
@Data
public class SystemPermissionCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private SystemPermissionKey key;  // CREATE_TASK (связь с enum)

    private String name;        // "Создание задачи" (для отображения)
    private String description; // описание
    private String group;        // "Задачи"
    private String icon;         // "fa-plus"
    private boolean builtIn;     // true
}
