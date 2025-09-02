package com.example.calendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    @JsonBackReference
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    @JsonBackReference
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "task")
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task")
    private List<TaskHistory> history = new ArrayList<>();
}
