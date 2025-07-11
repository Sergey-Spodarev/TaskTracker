package com.example.TaskTracker.service;

import com.example.TaskTracker.DTO.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@RequestMapping("/api/users")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO user) {

    }
}
