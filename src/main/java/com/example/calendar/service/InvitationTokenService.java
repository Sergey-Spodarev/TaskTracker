package com.example.calendar.service;

import com.example.calendar.DTO.InvitationTokenDTO;
import com.example.calendar.model.InvitationToken;
import com.example.calendar.model.User;
import com.example.calendar.repository.InvitationTokenRepository;
import com.example.calendar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class InvitationTokenService {
    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final CompanyEmailService companyEmailService;
    public InvitationTokenService(InvitationTokenRepository invitationTokenRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService, CompanyEmailService companyEmailService) {
        this.invitationTokenRepository = invitationTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.companyEmailService = companyEmailService;
    }

    public InvitationTokenDTO createInvitationToken(String email) {//мы просто создаём токен
        User user = userService.createPendingUser(email);
        InvitationToken invitationToken = new InvitationToken();
        //надо сделать отправку на почту сообщения
        invitationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitationToken.setUser(user);
        invitationToken.setToken(UUID.randomUUID().toString());

        InvitationToken saved = invitationTokenRepository.save(invitationToken);
        companyEmailService.sendInvitation(user.getCompany(),email, saved.getToken());

        return convertToDto(saved);
    }

    public InvitationTokenDTO completeRegistration(String token, String userName, String password) {//это когда пользователь переходит по ссылке и вводит имя и пароль
        InvitationToken invitationToken = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation token not found"));

        if (invitationToken.isUsed()) {throw new RuntimeException("Invitation token is used");}
        if (invitationToken.getExpiresAt().isBefore(LocalDateTime.now())) {throw new RuntimeException("Invitation token is expired");}
        User user = invitationToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        user.setUserName(userName);
        userRepository.save(user);

        invitationToken.setUsed(Boolean.TRUE);
        return convertToDto(invitationTokenRepository.save(invitationToken));
    }

    public InvitationTokenDTO convertToDto(InvitationToken invitationToken) {
        InvitationTokenDTO invitationTokenDTO = new InvitationTokenDTO();
        invitationTokenDTO.setToken(invitationToken.getToken());
        invitationTokenDTO.setUsed(invitationToken.isUsed());
        invitationTokenDTO.setExpiresAt(invitationToken.getExpiresAt());
        return invitationTokenDTO;
    }
}
