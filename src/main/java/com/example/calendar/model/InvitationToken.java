package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "invitation_tokens")
public class InvitationToken {
    @Id
    private Long id;

    private String token;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;
    private boolean used = false;

    @Override
    public String toString() {
        return "InvitationToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
