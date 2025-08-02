package com.example.calendar.controller;

import com.example.calendar.DTO.PasswordResetTokenDTO;
import com.example.calendar.service.PasswordResetTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//todo проверить на работа спасобность и ещё надо разобраться на счёт get а то я пока не понял почему они одинаковые ссылки
@Controller
@RequestMapping
public class PasswordResetTokenController {
    private final PasswordResetTokenService passwordResetTokenService;
    public PasswordResetTokenController(PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @GetMapping("/forgot-password")//html где вводят email
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetTokenDTO> forgotPassword(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(passwordResetTokenService.createRequestOnUpdatePassword(email));
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token) {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetTokenDTO> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {
        if (confirmPassword.equals(newPassword)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(passwordResetTokenService.updatePassword(token, newPassword));
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
