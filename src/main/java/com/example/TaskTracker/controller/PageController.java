package com.example.TaskTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin")
    public String adminPage() {
        return "admin"; // это admin.html в resources/templates/
    }

    @GetMapping("/user")
    public String userPage() {
        return "user"; // это user.html в resources/templates/
    }
}
