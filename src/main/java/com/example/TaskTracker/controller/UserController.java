package com.example.TaskTracker.controller;

import com.example.TaskTracker.DTO.UserDTO;
import com.example.TaskTracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/user")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserDTO userDTO) {
        UserDTO savedUser = userService.saveUser(userDTO);
        savedUser.setRole("USER");
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(savedUser);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserDTO> registerAdmin(@RequestBody @Valid UserDTO userDTO) {
        UserDTO savedUser = userService.saveUser(userDTO);
        savedUser.setRole("ADMIN");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
