package com.example.calendar.repository;

import com.example.calendar.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUser_Email(String email);
    Optional<PasswordResetToken> findByToken(String token);
}
