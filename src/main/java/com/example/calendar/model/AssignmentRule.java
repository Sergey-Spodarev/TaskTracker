package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AssignmentRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Role role;// Роль, для которой действует правило

    @ManyToOne
    @JoinColumn(name = "source_department_id", nullable = false)
    private Department sourceDepartment;//кто назначает

    @ManyToOne
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;//кому назначает

    private boolean allowed = true;//может ли назначить
}
