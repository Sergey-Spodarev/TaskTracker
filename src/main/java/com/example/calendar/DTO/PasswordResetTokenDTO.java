package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PasswordResetTokenDTO {
    private Long id;

    private String token;

    private LocalDateTime expiresAt;

    private boolean used;
}
