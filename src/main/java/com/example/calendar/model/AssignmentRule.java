package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "assignment_rules")
public class AssignmentRule {
    @Id
    @Column(name = "assignment_rule_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "source_department_id", nullable = false)
    private Department sourceDepartment;

    @ManyToOne
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    private boolean allowed = true;
}
