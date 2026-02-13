package com.example.calendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "code"}))
public class Role {
    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonBackReference
    private Department department;

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
