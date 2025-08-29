package com.example.calendar.controller;

import com.example.calendar.DTO.TaskCommentDTO;
import com.example.calendar.service.TaskCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/task")
public class TaskCommentController {
    private final TaskCommentService taskCommentService;
    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentDTO> createTaskComment(@RequestBody @Valid TaskCommentDTO taskCommentDTO, @PathVariable Long taskId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskCommentService.createTaskComment(taskCommentDTO, taskId));
    }

    @PutMapping("/{taskCommentId}/update")
    public ResponseEntity<TaskCommentDTO> updateTaskComment(@RequestBody @Valid TaskCommentDTO taskCommentDTO, @PathVariable Long taskCommentId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskCommentService.updateTaskComment(taskCommentDTO, taskCommentId));
    }
}
