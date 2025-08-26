package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users") //(опционально) – Указывает имя таблицы, если оно отличается от имени класса.
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "userName", nullable = false, unique = true)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Task> reporter = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Task> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)//mappedBy как называется строчка в классе с которым связываем
    @ToString.Exclude
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "company_id")//name как назовём столбец в БД
    private Company company;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne
    private InvitationToken invitationToken;

    @OneToMany(mappedBy = "autor_id")
    private List<TaskComment> taskComments = new ArrayList<>();

    @OneToMany(mappedBy = "changed_by_id")
    private List<TaskHistory> taskHistories = new ArrayList<>();
}
