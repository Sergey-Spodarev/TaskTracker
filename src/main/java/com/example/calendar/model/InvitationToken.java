package com.example.calendar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class InvitationToken {
    @Id
    private String token;

    @OneToOne
    private User user;

    private LocalDateTime expiresAt;
    private boolean used = false;
}
