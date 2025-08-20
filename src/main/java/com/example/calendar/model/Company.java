package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private Long INN;//идентификационный номер налогоплательщика

    @Column(unique = true)
    private String workEmail; // noreply@company.com

    private String smtpHost;  // smtp.yandex.ru
    private Integer smtpPort; // 587
    private String emailPassword; // App Password

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Department> departments = new ArrayList<>();
}
