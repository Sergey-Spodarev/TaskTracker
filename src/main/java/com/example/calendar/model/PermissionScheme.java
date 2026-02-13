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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_level_id")
    private RoleLevel roleLevel;

    @ManyToOne
    @JoinColumn(name = "company_id")// добавил данную связь надо докрутить присваивание, ну и конечно и в dto
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    private LocalDateTime createdAt;
}
