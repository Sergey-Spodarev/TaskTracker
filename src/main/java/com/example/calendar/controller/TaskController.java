package com.example.calendar.controller;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.TaskStatus;
import com.example.calendar.model.User;
import com.example.calendar.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/task")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/create")
    public ResponseEntity<TaskDTO> createTask(@RequestBody @Valid TaskDTO taskDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(taskDTO));
    }

    @PatchMapping("/{taskId}/assignee")
    public ResponseEntity<TaskDTO> changeAssignee(@PathVariable Long taskId, @RequestBody Long assigneeId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.changeAssignee(taskId, assigneeId));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> changeStatus(@PathVariable Long taskId, @RequestParam TaskStatus status){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.changeStatus(taskId, status));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTasksByAssignee());
    }

    @GetMapping("/created-by-me")
    public ResponseEntity<List<TaskDTO>> getTasksByReporter() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTasksByReporter());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByCompanyId(@PathVariable Long projectId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTasksByProjectId(projectId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<TaskDTO>> getTasks() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAllTasksInCompany());
    }
}
