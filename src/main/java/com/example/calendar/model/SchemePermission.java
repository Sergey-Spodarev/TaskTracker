package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "scheme_permissions")
public class SchemePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheme_permission_id")
    private Long id;

    private String permissionKey;
    private String description;

    private Integer minLevel;
    private Integer maxLevel;
}
