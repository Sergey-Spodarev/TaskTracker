package com.example.calendar.repository;

import com.example.calendar.model.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {
    boolean existsByToken(String token);
    Optional<InvitationToken> findByToken(String token);
}
