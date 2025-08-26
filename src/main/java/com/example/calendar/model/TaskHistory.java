package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TaskHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    private String fieldName;
    private String oldValue;
    private String newValue;

    private LocalDateTime changedAt = LocalDateTime.now();
}
