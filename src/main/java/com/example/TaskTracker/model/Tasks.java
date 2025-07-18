package com.example.TaskTracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endData;
    private String priority;

    @ManyToOne
    private Users creator;

    @ManyToOne
    private Users assignee;

    private String status;
    private String comment;
    private LocalDate updatedAt;
}
