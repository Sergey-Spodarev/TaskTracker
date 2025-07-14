package com.example.TaskTracker.controller;

import com.example.TaskTracker.DTO.TasksDTO;
import com.example.TaskTracker.DTO.UserDTO;
import com.example.TaskTracker.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
