package com.example.calendar.model;

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
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String INN;

    @Column(unique = true)
    private String workEmail;

    private String smtpHost;
    private Integer smtpPort;
    private String emailPassword;

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", INN='" + INN + '\'' +
                ", workEmail='" + workEmail +
                '}';
    }
}
