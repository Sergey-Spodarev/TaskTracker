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

    @GetMapping("/getAllUser")//todo проверить через Postman все решение ниже включая это
    public ResponseEntity<List<UserDTO>> getAllUser() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.getAllUsers());
    }

    @GetMapping("/getAllUsersWithTasks")
    public ResponseEntity<List<TasksDTO>> getAllTasks() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.getAllTasks());
    }

    @DeleteMapping("/delTask/{id}")
    public void delTask(@PathVariable Long id) {
        adminService.delTask(id);
    }

    @PutMapping("/updateTask")
    public ResponseEntity<TasksDTO> updateTask(@RequestBody TasksDTO tasksDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.updateTask(tasksDTO, email));
    }
}
