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

    private LocalDateTime start;

    private LocalDateTime end;

    private boolean allDay;

    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;
}
