package com.example.TaskTracker.controller;

import com.example.TaskTracker.DTO.UserDTO;
import com.example.TaskTracker.model.Users;
import com.example.TaskTracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/user")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserDTO userDTO) {
        userDTO.setRole("USER");
        UserDTO savedUser = userService.saveUser(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(savedUser);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserDTO> registerAdmin(@RequestBody @Valid UserDTO userDTO) {
        userDTO.setRole("ADMIN");
        UserDTO savedUser = userService.saveUser(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    @GetMapping("/test")
    public ResponseEntity<List<Users>> getAllUsers() {
        List<Users> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
