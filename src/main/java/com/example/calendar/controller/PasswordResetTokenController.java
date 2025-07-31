package com.example.calendar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class PasswordResetTokenController {
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
}
