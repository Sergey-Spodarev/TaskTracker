package com.example.TaskTracker.controller;

import com.example.TaskTracker.DTO.TasksDTO;
import com.example.TaskTracker.DTO.UserDTO;
import com.example.TaskTracker.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/addTask")
    public ResponseEntity<TasksDTO> addTask(@RequestBody TasksDTO tasksDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        TasksDTO addTask = adminService.saveTask(tasksDTO, email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addTask);
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<List<UserDTO>> getAllUser() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.getAllUsers());
    }

    @GetMapping("/getAllUsersWithTasks")
    public ResponseEntity<List<UserDTO>> getAllUsersWithTasks() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.getAllUsersWithTasks());
    }

    @DeleteMapping("/delTask")
    public void delTask(@RequestBody TasksDTO tasksDTO) {
        adminService.delTask(tasksDTO);
    }

    @PutMapping("/updateTask")
    public ResponseEntity<TasksDTO> updateTask(@RequestBody TasksDTO tasksDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body();
    }
}
