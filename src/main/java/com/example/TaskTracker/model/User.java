package com.example.TaskTracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    private Role role;
}
