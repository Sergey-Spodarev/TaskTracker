package com.example.TaskTracker.controller;

import com.example.TaskTracker.DTO.TasksUserDTO;
import com.example.TaskTracker.service.WorkerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/worker")
public class WorkerController {
    private final WorkerService workerService;
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping("/getTasks")
    public ResponseEntity<List<TasksUserDTO>> getTask() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workerService.getTask(email));
    }

    @PutMapping("/updateTask")
    public ResponseEntity<TasksUserDTO> updateTask(@RequestBody TasksUserDTO tasksUserDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workerService.updateTask(tasksUserDTO));
    }

}
