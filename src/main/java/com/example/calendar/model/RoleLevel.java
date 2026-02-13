package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role_levels")
public class RoleLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_level_id")
    private Long id;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private String levelName;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
