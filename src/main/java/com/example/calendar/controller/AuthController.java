package com.example.calendar.controller;

import com.example.calendar.DTO.LoginDto;
import com.example.calendar.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthController {
    @GetMapping("/task")
    public String showTasks() {
        return "task"; // → resources/templates/tasks.html
    }

    @GetMapping("/register")
    public String showCompleteRegistrationForm() {
        return "complete-registration"; // → resources/templates/complete-registration.html
    }

    @GetMapping("/lander")
    public String redirectToRegistration(@RequestParam String token) {
        return "redirect:/register?token=" + token;
    }
}
