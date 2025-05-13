package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean allDay;

    @ManyToOne//todo переделать на @OneToMany
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
}
