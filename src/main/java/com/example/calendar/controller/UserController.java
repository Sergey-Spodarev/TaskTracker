package com.example.calendar.controller;

import com.example.calendar.DTO.UserAssignmentDTO;
import com.example.calendar.DTO.UserDTO;
import com.example.calendar.DTO.UserWithRoleDTO;
import com.example.calendar.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserAssignmentDTO userAssignmentDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.assignUser(userAssignmentDTO));
    }

    @PostMapping("/update/dataUser")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.updateUser(userDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<UserWithRoleDTO> getCurrentUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUser());
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers());
    }

    @GetMapping("/{companyId}/awaiting")
    public ResponseEntity<List<UserWithRoleDTO>> getAwaitingRole(@PathVariable Long companyId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAwaitingRole(companyId));
    }
}
