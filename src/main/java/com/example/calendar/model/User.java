package com.example.calendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", unique = true)
    private String userName;

    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)//надо переделать логику чтобы при создании компании выдавался Владелец компании, а при приглашении пользователя Обычный сотрудник
    private SystemRole systemRole;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private Company company;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "role_level_id")
    private RoleLevel roleLevel;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private InvitationToken invitationToken;

    @Override
    public String toString() {
        return "User{" +
                "userId=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
