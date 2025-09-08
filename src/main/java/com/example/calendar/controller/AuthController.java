package com.example.calendar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/admin/tasks")
    public String adminTasks() {
        return "adminTask";
    }
}
