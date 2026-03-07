package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "custom_permission")
@Data
public class CustomPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_permission_id")
    private Long id;

    private String code;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createPermission;

    @ManyToMany
    private Set<SchemePermission> schemePermission = new HashSet<>();

    private LocalDateTime createTime;
}
