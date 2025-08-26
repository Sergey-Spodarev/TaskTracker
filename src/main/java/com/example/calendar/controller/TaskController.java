package com.example.calendar.controller;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class TaskController {
    private final TaskService eventService;
    public TaskController(TaskService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/add")
    public ResponseEntity<TaskDTO> addEvent(@RequestBody TaskDTO eventDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.saveEvent(eventDTO));
    }
    
    @GetMapping("/get")
    public List<TaskDTO> getEvents() {
        return eventService.getCurrentUserEvents();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestBody TaskDTO eventDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.updateEvent(eventDTO));
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
