package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "system_permission_definition")
@Data
public class SystemPermissionDefinition {
    @Id
    @Column(name = "system_permission_definition_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //todo возможно расписать как именно происходит связь
    @ManyToMany
    private Set<SystemPermissionCatalog> systemPermissionCatalog = new HashSet<>();

    private String code;
    private String name;
    private String description;
}//это комбо значения
