package com.example.calendar.controller;

import com.example.calendar.DTO.TaskHistoryDTO;
import com.example.calendar.service.TaskHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
public class TaskHistoryController {
    private final TaskHistoryService taskHistoryService;
    public TaskHistoryController(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskHistoryDTO>> getTaskHistory(@PathVariable Long taskId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskHistoryService.getAllTaskHistory(taskId));
    }
}
