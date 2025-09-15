package com.example.calendar.controller;

import com.example.calendar.DTO.TaskCommentDTO;
import com.example.calendar.service.TaskCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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

    @PutMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<TaskCommentDTO> updateTaskComment(@PathVariable Long taskId, @RequestBody @Valid TaskCommentDTO taskCommentDTO, @PathVariable Long commentId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskCommentService.updateTaskComment(taskCommentDTO, taskId, commentId));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentDTO>> getTaskComments(@PathVariable Long taskId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskCommentService.getTaskComment(taskId));
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<Void> deleteTaskComment(@PathVariable Long taskId, @PathVariable Long commentId) {
        taskCommentService.deleteTaskComment(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
