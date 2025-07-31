package com.example.calendar.service;

import com.example.calendar.DTO.PasswordResetTokenDTO;
import com.example.calendar.model.PasswordResetToken;
import com.example.calendar.model.User;
import com.example.calendar.repository.PasswordResetTokenRepository;
import com.example.calendar.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, UserRepository userRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public User checkHaveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public PasswordResetTokenDTO createRequestOnUpdatePassword(String email) {
        User user = checkHaveUser(email);
        String token = UUID.randomUUID().toString();
        sendMessage(email ,token);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        passwordResetToken.setUser(user);

        return convertToDTO(passwordResetTokenRepository.save(passwordResetToken));
    }

    private PasswordResetTokenDTO convertToDTO(PasswordResetToken passwordResetToken) {
        PasswordResetTokenDTO passwordResetTokenDTO = new PasswordResetTokenDTO();
        passwordResetTokenDTO.setToken(passwordResetToken.getToken());
        passwordResetTokenDTO.setExpiresAt(passwordResetToken.getExpiresAt());
        return passwordResetTokenDTO;
    }

    public void sendMessage(String email, String token){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Update password");
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        message.setText("To update your password, follow this link " + resetLink);
        mailSender.send(message);
    }

    public void updatePassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("Token not found with token: " + token));

        if (LocalDateTime.now().isAfter(passwordResetToken.getExpiresAt())){
            throw new RuntimeException("Токен просрочен");
        }

        if (passwordResetToken.isUsed()) {
            throw new RuntimeException("Токен уже использован");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
