package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "permission_schemes")
public class PermissionScheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_scheme_id")
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id")// добавил данную связь надо докрутить присваивание, ну и конечно и в dto
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "system_permission_definition_id")
    private SystemPermissionDefinition systemPermissionDefinition;

    @ManyToOne
    @JoinColumn(name = "custom_permission_id")
    private CustomPermission customPermission;

    private LocalDateTime createdAt;
}
